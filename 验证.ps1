# Stride 项目一键验证脚本
# 用法：确保后端(8081)和前端(5173)都已启动，然后在项目根目录执行：
#     powershell -ExecutionPolicy Bypass -File 验证.ps1
#
# 它会自动检查 12 项，全部 PASS 说明这份代码是好的。

$ErrorActionPreference = 'SilentlyContinue'
$api  = 'http://localhost:8081'
$web  = 'http://localhost:5173'
$pass = 0; $fail = 0

function Check($name, $ok, $detail) {
    if ($ok) { $script:pass++; Write-Host ("[PASS] " + $name + "  " + $detail) -ForegroundColor Green }
    else     { $script:fail++; Write-Host ("[FAIL] " + $name + "  " + $detail) -ForegroundColor Red }
}

Write-Host "===== Stride 项目验证开始 =====" -ForegroundColor Cyan
Write-Host ""

# 1. 后端存活
try {
    Invoke-WebRequest -Uri "$api/api/users/me" -TimeoutSec 10 -UseBasicParsing | Out-Null
    Check "后端服务" $true "8081 有响应"
} catch {
    $code = [int]$_.Exception.Response.StatusCode
    Check "后端服务" ($code -eq 401) "8081 返回 $code（401 表示服务正常）"
}

# 2. 前端存活
try {
    $r = Invoke-WebRequest -Uri "$web/" -TimeoutSec 10 -UseBasicParsing
    Check "前端服务" ($r.StatusCode -eq 200) "5173 返回 200"
} catch {
    Check "前端服务" $false "5173 无法访问"
}

# 3. 注册（同时验证参数校验、密码脱敏、id 自增）
$u = 'vtest' + (Get-Random -Maximum 99999)
$regOk = $false; $idOk = $false; $noPwd = $false
try {
    $body = "{`"username`":`"$u`",`"password`":`"123456`",`"email`":`"$u@t.com`"}"
    $reg = Invoke-WebRequest -Uri "$api/api/users/register" -Method POST -ContentType 'application/json' -Body $body -TimeoutSec 15 -UseBasicParsing
    $rj = [System.Text.Encoding]::UTF8.GetString($reg.RawContentStream.ToArray()) | ConvertFrom-Json
    $regOk = $true
    $idOk = ($rj.data.id -gt 0)
    $noPwd = -not ($rj.data.PSObject.Properties.Name -contains 'password')
} catch { }
Check "注册接口" $regOk "账号 $u"
Check "主键是正整数" $idOk "（负数 = @TableId 没配）"
Check "响应不含密码" $noPwd "（有 = UserVO 没生效）"

# 4. 登录
$tok = $null; $loginOk = $false
if ($regOk) {
    try {
        $lg = Invoke-WebRequest -Uri "$api/api/users/login" -Method POST -ContentType 'application/json' -Body "{`"username`":`"$u`",`"password`":`"123456`"}" -TimeoutSec 15 -UseBasicParsing
        $lj = [System.Text.Encoding]::UTF8.GetString($lg.RawContentStream.ToArray()) | ConvertFrom-Json
        $tok = $lj.data.token
        $loginOk = [bool]$tok
    } catch { }
}
Check "登录并签发 Token" $loginOk "（失败常见原因：jwt.secret 含符号或中文）"

# 5. 密码错误应拒绝
$rejOk = $false
if ($regOk) {
    try {
        Invoke-WebRequest -Uri "$api/api/users/login" -Method POST -ContentType 'application/json' -Body "{`"username`":`"$u`",`"password`":`"wrongpwd`"}" -TimeoutSec 15 -UseBasicParsing | Out-Null
    } catch { $rejOk = ([int]$_.Exception.Response.StatusCode -eq 401) }
}
Check "错误密码被拒绝" $rejOk "返回 401"

# 6. 普通用户越权应 403
$rbacOk = $false
if ($tok) {
    try {
        Invoke-WebRequest -Uri "$api/api/users" -Headers @{Authorization="Bearer $tok"} -TimeoutSec 15 -UseBasicParsing | Out-Null
    } catch { $rbacOk = ([int]$_.Exception.Response.StatusCode -eq 403) }
}
Check "普通用户越权被拦" $rbacOk "返回 403（若为 200 说明权限控制失效）"

# 7. 无 Token 应 401
$noTokOk = $false
try {
    Invoke-WebRequest -Uri "$api/api/users" -TimeoutSec 15 -UseBasicParsing | Out-Null
} catch { $noTokOk = ([int]$_.Exception.Response.StatusCode -eq 401) }
Check "无 Token 被拒绝" $noTokOk "返回 401"

# 8. 伪造 Token 应 401
$fakeOk = $false
try {
    Invoke-WebRequest -Uri "$api/api/users/me" -Headers @{Authorization="Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ4In0.fake"} -TimeoutSec 15 -UseBasicParsing | Out-Null
} catch { $fakeOk = ([int]$_.Exception.Response.StatusCode -eq 401) }
Check "伪造 Token 被拒绝" $fakeOk "返回 401"

# 9. 参数校验：空用户名应 400
$valOk = $false
try {
    Invoke-WebRequest -Uri "$api/api/users/register" -Method POST -ContentType 'application/json' -Body '{"username":"","password":"123456"}' -TimeoutSec 15 -UseBasicParsing | Out-Null
} catch { $valOk = ([int]$_.Exception.Response.StatusCode -eq 400) }
Check "空用户名被拦" $valOk "返回 400"

# 10. /me 接口
$meOk = $false
if ($tok) {
    try {
        $me = Invoke-WebRequest -Uri "$api/api/users/me" -Headers @{Authorization="Bearer $tok"} -TimeoutSec 15 -UseBasicParsing
        $meOk = ($me.StatusCode -eq 200)
    } catch { }
}
Check "查询当前用户 /me" $meOk "返回 200"

# 11-12. 前端关键代码存在
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$reqOk = Test-Path (Join-Path $root 'frontend/src/utils/request.js')
Check "前端 axios 封装" $reqOk "request.js 存在"
$guardOk = (Get-Content (Join-Path $root 'frontend/src/router/index.js') -Raw -ErrorAction SilentlyContinue) -match 'beforeEach'
Check "前端路由守卫" $guardOk "router 里有 beforeEach"

Write-Host ""
Write-Host "===== 结果：$pass 项通过，$fail 项失败 =====" -ForegroundColor Cyan
if ($fail -eq 0) {
    Write-Host "全部通过，这份代码可以放心使用。" -ForegroundColor Green
} else {
    Write-Host "有 $fail 项失败，先按《使用说明》和《补充说明》排查。" -ForegroundColor Yellow
}

$escapedCookies = $env:COOKIES_STR -replace ';', '\;' -replace '"', '\"'
$env:MAVEN_OPTS = "-Dcookies_str=`"$escapedCookies`" -Dai_dashscope_api_key=$($env:AI_DASHSCOPE_API_KEY -replace '\"','')"
mvn spring-boot:run
Pause
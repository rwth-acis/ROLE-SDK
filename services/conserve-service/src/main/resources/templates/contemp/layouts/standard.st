standard(param, static, bundle, literal, include) ::= <<
<!DOCTYPE HTML>
<html>
	<head>
		<title>$literal.title$</title>
		<style>
			body { font-family: sans-serif; font-size: 90%; }
			hr { height: 2px; border: 0px; background-color: #000; color: #000; }
		</style>
	</head>
	<body>
		<img src="$static.logo.small$" style="float: left; margin-right: .5em;"/>
		<h1>$literal.title$</h1>
		<hr/>
		$include.body$
		<hr/>
	</body>
</html>
>>
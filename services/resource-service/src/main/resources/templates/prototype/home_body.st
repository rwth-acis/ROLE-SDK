home_body(param, static, bundle, literal, include) ::= <<
<h2 style="border-bottom: 1px solid #00cc55;">Learning Spaces</h2>
<ul style="margin: 0px; padding-left: 1em;">
$param.space.title.keys:{space|
	<li><a href="$space$">$param.space.title.(space)$</a></li>
}$
</ul>
<div><em><span style="color: #00cc55; font-weight: bold; font-size: 16px;">+</span> <a href="/spaces">Create a new space</a></em></div> 

<h2 style="margin-top: 3em; border-bottom: 1px solid #00cc55;">User Profiles</h2>
<ul style="margin: 0px; padding-left: 1em;">
$param.user.title.keys:{user|
	<li><a href="$user$">$param.user.title.(user)$</a></li>
}$
</ul>
>>
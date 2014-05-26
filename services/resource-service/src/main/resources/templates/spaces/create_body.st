create_body(param, static, bundle, literal, include) ::= <<
<style>
	#box { background: #fff; width: 30em; height: 30em; position: absolute; top: 50%; left: 50%;
	    margin-top: -15em; margin-left: -15em; border: 1px solid #ccc; padding: 20px;
	    text-align: left; }
	#create { position: relative; left: 50%; margin-left: -170px; text-align: center; background: url($static.background.shade$); color: #000; padding: 20px; font-size: 20px; width: 300px; cursor: pointer; border: 1px solid #000; }
	#create:hover { background: url($static.background.highlight$); text-decoration: underline; color: #000; }
</style>

<form id="box" action="spaces" method="post">
	<div id="create" onclick="document.getElementById('box').submit();">Create new space</div> 
	<h3 style="margin-top: 30px;">If you like, you can specify any of the following:</h3> 
	<ul> 
		<li>The space's id is <input size="20" style="border: 1px solid #ccc;" name="openapp.rdfs.label" value="$param.newspace.id$"/></li> 
		<li>The space's title is <input size="20" style="border: 1px solid #ccc;" name="openapp.dcterms.title" value="English learning"/></li> 
		<!--<li>In the new space, I want to <select><option>learn a language</option></select></li> 
		<li>My preferred color is <select><option>ROLE Green</option></select></li>
		<li>I allow <select><option>members</option></select> to participate the space</li> 
		<li>I allow <select><option>anyone</option></select> to view the space</li>-->
  	</ul>
	<p>Then click on the create button above to enter your new space.</p> 
	<input type="hidden" name="openapp.ns.rdf" value="http://www.w3.org/1999/02/22-rdf-syntax-ns#"/>
	<input type="hidden" name="openapp.ns.rdfs" value="http://www.w3.org/2000/01/rdf-schema#"/>
	<input type="hidden" name="openapp.ns.dcterms" value="http://purl.org/dc/terms/"/>
	<input type="hidden" name="openapp.rdf.predicate" value="http://www.role-project.eu/rdf/space"/>
	<input type="hidden" name="openapp.rdf.type" value="http://www.role-project.eu/rdf/Space"/>
</form>
>>
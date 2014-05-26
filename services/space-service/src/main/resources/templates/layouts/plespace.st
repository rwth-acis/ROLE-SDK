{
	"userName": "$param.user.name$",
	"userURI": "$param.user.uri$",
	"spaceTitle": "$param.space.title$",
	"spaceURI": "$param.context.uri$",
	"tools": {
		$param.tool.src.keys:{tool|
			"$tool$": {
				"uri": "$param.tool.uri.(tool)$",
				"src": "$param.tool.src.(tool)$",
				"title": "$param.tool.title.(tool)$",
				"height": $param.tool.height.(tool)$
			}
		};separator=", "$
	},
	"ptools": {
		$param.personal.src.keys:{tool|
			"$tool$": {
				"uri": "$param.personal.uri.(tool)$",
				"src": "$param.personal.src.(tool)$",
				"title": "$param.personal.title.(tool)$",
				"height": $param.personal.height.(tool)$
			}
		};separator=", "$
	}
}
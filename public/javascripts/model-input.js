/*
 * Copyright (C) 2014 mPower Social Enterprise LTD. (http://www.mpower-social.com)
 */

//global fields
/** list of array model*/
var arrayModels = new Array();//new Array("user","testInput")
/** serial of input fields of array model */
var fieldSerialMap={}; //= {"user.testField": 0, "testField": 101};

function setArrayModels(paramModels) {
	return setArrayModelsByTagName(paramModels, 'select') 
			&& setArrayModelsByTagName(paramModels, 'input') ;	
}

/**
 * Set array models input fields with its index
 * e.g. 'user.id' to 'users[0].id'
 * */
function setArrayModelsByTagName(paramModels, tagName) {
	var inputs = document.getElementsByTagName(tagName);
	
	arrayModels = paramModels;
	
	for(var i = 0; i < inputs.length; i++){
		
		var anc = ancestor(inputs[i], "div.serial")
		var sl = 0;
		if(anc != null) {
			var val = anc.getAttribute("value");
			if(val) {
				sl =val;
			}
		}
		
		var fieldName =  inputs[i].getAttribute('name');
		if(fieldName != null) {
			var model = fieldName.split('.')[0];
			if(arrayModels.indexOf(model)>-1) {
				var newName = fieldName.replace('.', 's['+ sl + '].');
				inputs[i].setAttribute('name', newName);	
			}	
		}
	}
	return true;
}

/**
 * Return ancestor node
 * */
function ancestor(node, match){
  if(!node) {
    return null;
  }
  else if(!node.nodeType || typeof(match) != 'string') {
    return node;
  }
  if((match = match.split('.')).length === 1) {
    match.push(null);
  }
  else if(!match[0]) {
    match[0] = null;
  }
  do {
    if
    (
      (
        !match[0]
        ||
        match[0].toLowerCase() == node.nodeName.toLowerCase())
      &&
      (
        !match[1]
        ||
        new RegExp('( |^)(' + match[1] + ')( |$)').test(node.className)
      )
    )
    {
      break;
    }
  }
  while(node = node.parentNode);
 
  return node;
}
dojo.provide("dojo.lang.type");

dojo.require("dojo.lang.common");

dojo.lang.whatAmI = function(value) {
	dojo.deprecated("dojo.lang.whatAmI", "use dojo.lang.getType instead", "0.5");
	return dojo.lang.getType(value);
}

dojo.lang.getType = function(value) {
	try {
		if(dojo.lang.isArray(value)) { return "array"; }
		if(dojo.lang.isFunction(value)) { return "function"; }
		if(dojo.lang.isString(value)) { return "string"; }
		if(dojo.lang.isNumber(value)) { return "number"; }
		if(dojo.lang.isBoolean(value)) { return "boolean"; }
		if(dojo.lang.isAlien(value)) { return "alien"; }
		if(dojo.lang.isUndefined(value)) { return "undefined"; }
		// FIXME: should this go first?
		for(var name in dojo.lang.whatAmI.custom) {
			if(dojo.lang.whatAmI.custom[name](value)) {
				return name;
			}
		}
		if(dojo.lang.isObject(value)) { return "object"; }
	} catch(E) {}
	return "unknown";
}
/*
 * dojo.lang.whatAmI.custom[typeName] = someFunction
 * will return typeName is someFunction(value) returns true
 */
dojo.lang.whatAmI.custom = {};

/**
 * Returns true for values that commonly represent numbers.
 *
 * Examples:
 * <pre>
 *   dojo.lang.isNumeric(3);                 // returns true
 *   dojo.lang.isNumeric("3");               // returns true
 *   dojo.lang.isNumeric(new Number(3));     // returns true
 *   dojo.lang.isNumeric(new String("3"));   // returns true
 *
 *   dojo.lang.isNumeric(3/0);               // returns false
 *   dojo.lang.isNumeric("foo");             // returns false
 *   dojo.lang.isNumeric(new Number("foo")); // returns false
 *   dojo.lang.isNumeric(false);             // returns false
 *   dojo.lang.isNumeric(true);              // returns false
 * </pre>
 */
dojo.lang.isNumeric = function(value){
	return (!isNaN(value) && isFinite(value) && (value != null) &&
			!dojo.lang.isBoolean(value) && !dojo.lang.isArray(value) &&
			!/^\s*$/.test(value));
}

/**
 * Returns true for any literal, and for any object that is an 
 * instance of a built-in type like String, Number, Boolean, 
 * Array, Function, or Error.
 */
dojo.lang.isBuiltIn = function(value){
	return (dojo.lang.isArray(value)		|| 
			dojo.lang.isFunction(value)	|| 
			dojo.lang.isString(value)		|| 
			dojo.lang.isNumber(value)		|| 
			dojo.lang.isBoolean(value)		|| 
			(value == null)				|| 
			(value instanceof Error)		|| 
			(typeof value == "error") );
}

/**
 * Returns true for any object where the value of the 
 * property 'constructor' is 'Object'.  
 * 
 * Examples:
 * <pre>
 *   dojo.lang.isPureObject(new Object()); // returns true
 *   dojo.lang.isPureObject({a: 1, b: 2}); // returns true
 * 
 *   dojo.lang.isPureObject(new Date());   // returns false
 *   dojo.lang.isPureObject([11, 2, 3]);   // returns false
 * </pre>
 */
dojo.lang.isPureObject = function(value){
	return ((value != null) && dojo.lang.isObject(value) && value.constructor == Object);
}

/**
 * Given a value and a datatype, this method returns true if the
 * type of the value matches the datatype. The datatype parameter
 * can be an array of datatypes, in which case the method returns
 * true if the type of the value matches any of the datatypes.
 *
 * Examples:
 * <pre>
 *   dojo.lang.isOfType("foo", String);                // returns true
 *   dojo.lang.isOfType(12345, Number);                // returns true
 *   dojo.lang.isOfType(false, Boolean);               // returns true
 *   dojo.lang.isOfType([6, 8], Array);                // returns true
 *   dojo.lang.isOfType(dojo.lang.isOfType, Function); // returns true
 *   dojo.lang.isOfType({foo: "bar"}, Object);         // returns true
 *   dojo.lang.isOfType(new Date(), Date);             // returns true
 *
 *   dojo.lang.isOfType("foo", "string");                // returns true
 *   dojo.lang.isOfType(12345, "number");                // returns true
 *   dojo.lang.isOfType(false, "boolean");               // returns true
 *   dojo.lang.isOfType([6, 8], "array");                // returns true
 *   dojo.lang.isOfType(dojo.lang.isOfType, "function"); // returns true
 *   dojo.lang.isOfType({foo: "bar"}, "object");         // returns true
 *   dojo.lang.isOfType(xxxxx, "undefined");             // returns true
 *   dojo.lang.isOfType(null, "null");                   // returns true
 *
 *   dojo.lang.isOfType("foo", [Number, String, Boolean]); // returns true
 *   dojo.lang.isOfType(12345, [Number, String, Boolean]); // returns true
 *   dojo.lang.isOfType(false, [Number, String, Boolean]); // returns true
 *
 *   dojo.lang.isOfType(null, Date, {optional: true} );    // returns true
 * </pre>
 *
 * @param	value	Any literal value or object instance.
 * @param	type	A class of object, or a literal type, or the string name of a type, or an array with a list of types.
 * @return	Returns a boolean
 */
dojo.lang.isOfType = function(value, type, keywordParameters) {
	var optional = false;
	if (keywordParameters) {
		optional = keywordParameters["optional"];
	}
	if (optional && ((value === null) || dojo.lang.isUndefined(value))) {
		return true;
	}
	if(dojo.lang.isArray(type)){
		var arrayOfTypes = type;
		for(var i in arrayOfTypes){
			var aType = arrayOfTypes[i];
			if(dojo.lang.isOfType(value, aType)) {
				return true;
			}
		}
		return false;
	}else{
		if(dojo.lang.isString(type)){
			type = type.toLowerCase();
		}
		switch (type) {
			case Array:
			case "array":
				return dojo.lang.isArray(value);
				break;
			case Function:
			case "function":
				return dojo.lang.isFunction(value);
				break;
			case String:
			case "string":
				return dojo.lang.isString(value);
				break;
			case Number:
			case "number":
				return dojo.lang.isNumber(value);
				break;
			case "numeric":
				return dojo.lang.isNumeric(value);
				break;
			case Boolean:
			case "boolean":
				return dojo.lang.isBoolean(value);
				break;
			case Object:
			case "object":
				return dojo.lang.isObject(value);
				break;
			case "pureobject":
				return dojo.lang.isPureObject(value);
				break;
			case "builtin":
				return dojo.lang.isBuiltIn(value);
				break;
			case "alien":
				return dojo.lang.isAlien(value);
				break;
			case "undefined":
				return dojo.lang.isUndefined(value);
				break;
			case null:
			case "null":
				return (value === null);
				break;
			case "optional":
				dojo.deprecated('dojo.lang.isOfType(value, [type, "optional"])', 'use dojo.lang.isOfType(value, type, {optional: true} ) instead', "0.5");
				return ((value === null) || dojo.lang.isUndefined(value));
				break;
			default:
				if (dojo.lang.isFunction(type)) {
					return (value instanceof type);
				} else {
					dojo.raise("dojo.lang.isOfType() was passed an invalid type");
				}
				break;
		}
	}
	dojo.raise("If we get here, it means a bug was introduced above.");
}

/*
 * 	From reflection code, part of merge.
 *	TRT 2006-02-01
 */
dojo.lang.getObject=function(/* String */ str){
	//	summary
	//	Will return an object, if it exists, based on the name in the passed string.
	var parts=str.split("."), i=0, obj=dj_global; 
	do{ 
		obj=obj[parts[i++]]; 
	}while(i<parts.length&&obj); 
	return (obj!=dj_global)?obj:null;	//	Object
}

dojo.lang.doesObjectExist=function(/* String */ str){
	//	summary
	//	Check to see if object [str] exists, based on the passed string.
	var parts=str.split("."), i=0, obj=dj_global; 
	do{ 
		obj=obj[parts[i++]]; 
	}while(i<parts.length&&obj); 
	return (obj&&obj!=dj_global);	//	boolean
}

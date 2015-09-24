SomeScript = function() {
	this.s = "wow";
};

SomeScript.prototype = new Object();

SomeScript.constructor = SomeScript;

SomeScript.prototype.doSth = function() {
	console.log(this.s);
};
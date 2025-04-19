const modClassLoader = Vars.mods.mainLoader();

function classForName(name) {
	try {
		return Class.forName(name, true, modClassLoader);
	} catch (e) {
		Log.err(e);
		return null;
	}
}

function importCls(name) {
	return importClass(new Packages.rhino.NativeJavaClass(Vars.mods.scripts.scope, Class.forName(name, true, Vars.mods.mainLoader())))
}

const _buffer = _interface.getConsole().logBuffer;
const _defaultMethods = new java.lang.Object();
const _nativeContains = (array, name) => {
	for (i in array) {
		if (i.equals(name)) return true;
	}
	return false;
}

function readString(str, fallback) {
    let str = Vars.tree.get(str);

    if(!str.exists()) return fallback;

    try{
        return str.readString();
    }catch(e){
        return fallback;
    }

    return str;
}

function readString(str) {
    return readString(str, "invalid/nonexistent file")
}


function NCHelp() {
	let help = readString("console/startup.js-help");
	
	println(help);
}

const append = text => {
	_buffer.append(text);
	return null;
};
const println = text => {
	_buffer.append(text).append("\n");
	return null;
};
const backread = () => JSInterface.getConsole().backread();

const NewConsole = JSInterface;

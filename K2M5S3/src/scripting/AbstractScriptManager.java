/*
 * ArcStory Project
 * √÷¡÷ø¯ sch2307@naver.com
 * ¿Ã¡ÿ junny_adm@naver.com
 * øÏ¡ˆ»∆ raccoonfox69@gmail.com
 * ∞≠¡§±‘ ku3135@nate.com
 * ±Ë¡¯»´ designer@inerve.kr
 */

package scripting;

import java.io.File;
import java.io.IOException;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import client.MapleClient;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.script.ScriptException;

public abstract class AbstractScriptManager {
    
    protected Invocable getInvocable(String path, MapleClient c) {
        try {
            path = "Scripts/" + path;
            File scriptFile = new File(path);
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
            if (c != null)
                c.setScriptEngine(path, engine);
            if (scriptFile.exists()) {
                try (Stream<String> stream = Files.lines(scriptFile.toPath(), Charset.forName("EUC-KR"))) { 
                    String lines = "load('nashorn:mozilla_compat.js');"; 
                    lines += stream.collect(Collectors.joining(System.lineSeparator())); 
                    engine.eval(lines); 
                } catch (final ScriptException | IOException t) { 
                    return null;
                } 
            } else {
                return null;
            }
            return (Invocable) engine;
	} catch (Exception e) {
            e.printStackTrace();
	    return null;
	}
    }
}

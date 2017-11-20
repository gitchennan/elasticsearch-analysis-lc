package com.hankcs.test.proxy;

import org.junit.Test;

import javax.script.*;

public class ScriptTest {
    @Test
    public void jsScriptTest() throws Exception {
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        ScriptEngine engine = scriptEngineManager.getEngineByName("javascript");
        System.out.println(engine.getClass());

        for (ScriptEngineFactory scriptEngineFactory : scriptEngineManager.getEngineFactories()) {
            System.out.println(scriptEngineFactory.getEngineName() + ":" + scriptEngineFactory.getLanguageVersion());
        }

        Compilable compilableEngine = (Compilable) engine;
        String script = "a + 1";

        Bindings bindings = engine.createBindings();
        bindings.put("a", 1);

        CompiledScript compiledScript = compilableEngine.compile(script);
        Double c = (Double) compiledScript.eval(bindings);

        System.out.println(c);

        long r = 100;
        long start = System.currentTimeMillis();
        for (int i = 0; i < r; i++) {
            bindings = engine.createBindings();
            bindings.put("a", i);
            c = (Double) compiledScript.eval(bindings);
//            System.out.println(c);
        }
        long end = System.currentTimeMillis();
        System.out.println(((end - start) / (double) r) + " ms");
    }
}

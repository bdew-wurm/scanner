package net.bdew.wurm.scanner;

import com.wurmonline.client.renderer.gui.HeadsUpDisplay;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.PreInitable;
import org.gotti.wurmunlimited.modloader.interfaces.WurmClientMod;

import java.util.logging.Logger;

public class ScannerMod implements WurmClientMod, Initable, PreInitable {
    private static Logger logger = Logger.getLogger("ScannerMod");
    public static HeadsUpDisplay hud;

    @Override
    public void init() {
        HookManager.getInstance().registerHook("com.wurmonline.client.renderer.gui.HeadsUpDisplay", "init", "(II)V", () -> (proxy, method, args) -> {
            method.invoke(proxy, args);
            hud = (HeadsUpDisplay) proxy;
            return null;
        });
    }

    @Override
    public void preInit() {
        try {
            ClassPool classPool = HookManager.getInstance().getClassPool();

            // Disable secure strings bullshit
            CtClass ctSecureStrings = classPool.getCtClass("com.wurmonline.client.util.SecureStrings");
            ctSecureStrings.getConstructor("(Ljava/lang/String;)V").setBody("this.chars = $1.toCharArray();");
            ctSecureStrings.getMethod("toString", "()Ljava/lang/String;").setBody("return new String(this.chars);");

            // setup command handler
            CtClass ctWurmConsole = classPool.getCtClass("com.wurmonline.client.console.WurmConsole");
            ctWurmConsole.getMethod("handleDevInput", "(Ljava/lang/String;[Ljava/lang/String;)Z").insertBefore(
                    "if (net.bdew.wurm.scanner.CommandHandler.handleInput($1,$2)) return true;"
            );

            // Intercept outline rendering
            classPool.getCtClass("com.wurmonline.client.renderer.WorldRender").getMethod("renderWorld", "(II)V")
                    .instrument(new ExprEditor() {
                        @Override
                        public void edit(MethodCall m) throws CannotCompileException {
                            if (m.getMethodName().equals("renderPickedItem")) {
                                m.replace("this.renderPickedItem(this.queuePick); net.bdew.wurm.scanner.OutlineRender.render(this, this.queuePick);");
                            }
                        }
                    });

        } catch (CannotCompileException | NotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

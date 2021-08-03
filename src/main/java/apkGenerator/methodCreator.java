package apkGenerator;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.SootMethodRefImpl;
import soot.Type;
import soot.Unit;
import soot.UnitPatchingChain;
import soot.VoidType;
import soot.jimple.AssignStmt;
import soot.jimple.BreakpointStmt;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.MonitorStmt;
import soot.jimple.NopStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.SwitchStmt;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThrowStmt;
import soot.jimple.VirtualInvokeExpr;

public class methodCreator {
	
	public static void createInitMethod(SootClass ifClass) {
		SootClass activity = Scene.v().loadClassAndSupport("android.app.Activity");
		SootMethod tmp = activity.getMethodByName("<init>");
		SootMethod initMethod = new SootMethod(tmp.getName(), tmp.getParameterTypes(), tmp.getReturnType(), 0);
		ifClass.addMethod(initMethod);
		JimpleBody initBody = new JimpleBody(initMethod);
		UnitPatchingChain units = initBody.getUnits();
		// ifClass r0
		Local r0 = Jimple.v().newLocal("r0", ifClass.getType());
		initBody.getLocals().add(r0);
		// r0 := @this : ifClass
		units.add(Jimple.v().newIdentityStmt(r0, Jimple.v().newThisRef(ifClass.getType())));
		// Specialinvoke r0.<android.app.Activity : void <init()>();
		SpecialInvokeExpr specialExpr = Jimple.v().newSpecialInvokeExpr(r0, tmp.makeRef());
		units.add(Jimple.v().newInvokeStmt(specialExpr));
		// return
		units.add(Jimple.v().newReturnVoidStmt());
		initBody.validate();
		System.out.println(initBody);
	}
	
	public static void createOnCreateMethod(SootClass ifClass) {
		SootClass activity = Scene.v().loadClassAndSupport("android.app.Activity");
		SootMethod tmp = activity.getMethodByName("onCreate");
		SootMethod onCreateMethod = new SootMethod(tmp.getName(), tmp.getParameterTypes(), tmp.getReturnType(), 0);
		ifClass.addMethod(onCreateMethod);
		JimpleBody onCreateBody = new JimpleBody(onCreateMethod);
		UnitPatchingChain units = onCreateBody.getUnits();
		// ifClass r0
		Local r0 = Jimple.v().newLocal("r0", ifClass.getType());
		onCreateBody.getLocals().add(r0);
		// android.os.bundle $r1
		Local $r1 = Jimple.v().newLocal("$r1", Scene.v().getType("android.os.bundle"));
		onCreateBody.getLocals().add($r1);
		// r0 := @this: ifClass
		units.add(Jimple.v().newIdentityStmt(r0, Jimple.v().newThisRef(ifClass.getType())));
		// $r1 := @parameter0: android.os.Bundle
		units.add(Jimple.v().newIdentityStmt($r1, Jimple.v().newParameterRef(Scene.v().getType("android.os.bundle"), 0)));
		// invoke r0.ifMethod()
		SootMethod ifMeth = Scene.v().getMethod("ifMethod");
		VirtualInvokeExpr inv = Jimple.v().newVirtualInvokeExpr(r0, ifMeth.makeRef());
		units.add(Jimple.v().newInvokeStmt(inv));
		onCreateBody.validate();
	}
	
	public static void createIfMethod(SootClass ifClass, Set<Local> localSet, List<Stmt> stmtList) {
		List<Type> typeList = new ArrayList<Type>();
		SootMethod ifMethod = new SootMethod("ifMethod", typeList, VoidType.v(), Modifier.PUBLIC);
		ifClass.addMethod(ifMethod);
		JimpleBody ifMethodBody = new JimpleBody(ifMethod);
		addLocals(localSet, ifMethodBody);
		addStmt(ifMethodBody, stmtList);
		solveTargets(ifMethodBody, stmtList);
		System.out.println(ifMethodBody);
		ifMethodBody.validate();
	}
	
	private static void addLocals(Set<Local> s, JimpleBody b) {
		for(Local loc : s) {
			Local tmp = Jimple.v().newLocal(loc.getName(), loc.getType());
			b.getLocals().add(tmp);
		}
	}
	
	private static void addStmt(JimpleBody b, List<Stmt> l) {
		UnitPatchingChain units = b.getUnits();
        List<Unit> generatedUnits = new ArrayList<>();
        // List de Pair<Stmt,Stmt>
		for(Stmt s : l) {
			//System.out.println(s);
			/* Assignement */
			if (s instanceof AssignStmt) {
				generatedUnits.add(stmtCreator.createAssignStmt(s, b));
			}
			/* Identification */
			else if (s instanceof IdentityStmt) {
				generatedUnits.add(stmtCreator.createIdentity(s, b));
			}
			/* Go to */
			else if(s instanceof GotoStmt) {
				generatedUnits.add(stmtCreator.createGoToStmt(s, b));
			}
			/* if */
			else if (s instanceof IfStmt) {
				Stmt createIfStmt = stmtCreator.createIfStmt(s, b);
				// createPair(s, createIfStmt)
				// add new pair
				generatedUnits.add(stmtCreator.createIfStmt(s, b));
			}
			/* invoke */
			else if (s instanceof InvokeStmt) {
				generatedUnits.add(stmtCreator.createInvokeStmt(s, b));
			}
			/* switch */
			else if (s instanceof SwitchStmt) {
				generatedUnits.add(stmtCreator.createSwitchStmt(s, b));
			}
			/* Monitor */
			else if (s instanceof MonitorStmt) {
				generatedUnits.add(stmtCreator.createMonitorStmt(s, b));
			}
			/* return */
			else if (s instanceof ReturnStmt) {
				generatedUnits.add(stmtCreator.createReturnStmt(s, b));
			}
			/* throw */
			else if (s instanceof ThrowStmt) {
				generatedUnits.add(stmtCreator.createThrowStmt(s, b));
			}
			/* Breakpoint */
			else if(s instanceof BreakpointStmt) {
				generatedUnits.add(Jimple.v().newBreakpointStmt());
			}
			/* Nop */
			else if(s instanceof NopStmt) {
				generatedUnits.add(stmtCreator.createNopStmt());
			}
		}
		solveTargets(b, l);
		units.addAll(generatedUnits);
	}
	
	/* LISTE DE TUPLE POUR GERER */
	private static void solveTargets(JimpleBody b, List<Stmt> block) {
		for(Unit u : b.getUnits()) {
			Stmt s = (Stmt) u;
			if(s instanceof IfStmt) {
				IfStmt st = (IfStmt) s;
				Unit target = st.getTarget();
				for(Unit us : b.getUnits()) {
					if(target.toString().equals(us.toString())) {
						((IfStmt) s).setTarget(us);
					}
				}
			} else if(s instanceof TableSwitchStmt) {
				TableSwitchStmt st = (TableSwitchStmt) s;
				for(Unit target : st.getTargets()) {
					for(Unit us : b.getUnits()) {
						if(target.toString().equals(us.toString())) {
							((TableSwitchStmt) s).setDefaultTarget(us);
						}
					}
				}
			} else if(s instanceof LookupSwitchStmt) {
				LookupSwitchStmt st = (LookupSwitchStmt) s;
				for(Unit target : st.getTargets()) {
					for(Unit us : b.getUnits()) {
						if(target.toString().equals(us.toString())) {
							((LookupSwitchStmt) s).setDefaultTarget(target);
						}
					}
				}
			} else if(s instanceof GotoStmt) {
				GotoStmt st = (GotoStmt) s;
				Unit target = st.getTarget();
				for(Unit us : b.getUnits()) {
					if(target.toString().equals(us.toString())) {
						((GotoStmt) s).setTarget(us);
					}
				}	
			}
		}
		/* ReturnVoidStmt nullReturn = Jimple.v().newReturnVoidStmt(); 
				b.getUnits().add(nullReturn);
				st.setTarget(nullReturn); */
	}
}

package ch.so.agi.gretl.util.metapublisher;

import ch.interlis.ili2c.metamodel.AttributeDef;
import ch.interlis.ili2c.metamodel.CompositionType;
import ch.interlis.ili2c.metamodel.Table;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.ili2c.metamodel.Type;

@Deprecated
public class Ili2cUtility {
    
    public static boolean isPureChbaseMultiSurface(TransferDescription td,AttributeDef attr) {
        Type typeo=attr.getDomain();
        if(typeo instanceof CompositionType){
            CompositionType type=(CompositionType)typeo;
            Table struct=type.getComponentType();
            Table root=(Table) struct.getRootExtending();
            if(root==null){
                root=struct;
            }
            String containerQName=root.getContainer().getScopedName(null);
            if(containerQName.equals(IliNames.CHBASE1_GEOMETRYCHLV03) || containerQName.equals(IliNames.CHBASE1_GEOMETRYCHLV95)){
                if(root.getName().equals(IliNames.CHBASE1_GEOMETRY_MULTISURFACE)){
                    java.util.Iterator it=struct.getAttributesAndRoles2();
                    int c=0;
                    while(it.hasNext()){
                        it.next();
                        c++;
                    }
                    if(c==1){
                        // only one attribute
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public static boolean isPureChbaseMultiLine(TransferDescription td,AttributeDef attr) {
        Type typeo=attr.getDomain();
        if(typeo instanceof CompositionType){
            CompositionType type=(CompositionType)typeo;
            Table struct=type.getComponentType();
            Table root=(Table) struct.getRootExtending();
            if(root==null){
                root=struct;
            }
            String containerQName=root.getContainer().getScopedName(null);
            if(containerQName.equals(IliNames.CHBASE1_GEOMETRYCHLV03) || containerQName.equals(IliNames.CHBASE1_GEOMETRYCHLV95)){
                if(root.getName().equals(IliNames.CHBASE1_GEOMETRY_MULTILINE) || root.getName().equals(IliNames.CHBASE1_GEOMETRY_MULTIDIRECTEDLINE)){
                    java.util.Iterator it=struct.getAttributesAndRoles2();
                    int c=0;
                    while(it.hasNext()){
                        it.next();
                        c++;
                    }
                    if(c==1){
                        // only one attribute
                        return true;
                    }
                }
            }
        }
        return false;
    }

}

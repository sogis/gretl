package ch.so.agi.gretl.steps.publisher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO representing the flat Gradle inputs exactly as provided (strings, booleans, file paths, etc.).
 * Does basic validation asserting a proper combination of arguments was given.
 */
class RawPublisherArgs {
    private String dbDatabase;
    private String dbSchema;
    private String dbIliIdent_Type;
    private ArrayList<String> dbIliIdent_Values;
    private String dbIliIdent_RegEx;
    private Boolean dbMergeToSingleXtf;
    private String xtfFile_FolderPath;
    private String xtfFilename_Regex;
    private ArrayList<String> xtfFilename_List;

    private PublishMode publishMode;
    private IliIdentType dbIliIdent_TypeEnum;

    enum PublishMode{
        dbIdentvaluesList,
        dbIdentvaluesRegex,
        xtfFilesList,
        xtfFilesRegex
    }

    enum IliIdentType{
        model,
        topic,
        basket,
        dataset
    }

    RawPublisherArgs(String dbDatabase,
                            String dbSchema,
                            String dbIliIdent_Type,
                            ArrayList<String> dbIliIdent_Values,
                            String dbIliIdent_RegEx,
                            Boolean dbMergeToSingleXtf,
                            String xtfFile_FolderPath,
                            String xtfFilename_Regex,
                            ArrayList<String> xtfFilename_List) {


        this.dbDatabase = dbDatabase;
        this.dbSchema = dbSchema;
        this.dbIliIdent_Type = dbIliIdent_Type;
        this.dbIliIdent_Values = dbIliIdent_Values;
        this.dbIliIdent_RegEx = dbIliIdent_RegEx;
        this.dbMergeToSingleXtf = dbMergeToSingleXtf;
        this.xtfFile_FolderPath = xtfFile_FolderPath;
        this.xtfFilename_Regex = xtfFilename_Regex;
        this.xtfFilename_List = xtfFilename_List;

        validateArgumentCombination();
    }

    private void validateArgumentCombination(){
        boolean sourceXtf = (xtfFile_FolderPath != null && !xtfFile_FolderPath.isEmpty());

        if(sourceXtf){
            assertAllDbArgsNull();
            assertValidXtfArgs();
        }
        else {
            assertAllXtfArgsNull();
            assertValidDbArgs();
        }

        assignPublisherMode(sourceXtf);
    }

    private void assignPublisherMode(boolean sourceIsXtf) {
        if(sourceIsXtf){
            if(xtfFilename_List == null || xtfFilename_List.isEmpty())
                this.publishMode = PublishMode.xtfFilesRegex;
            else
                this.publishMode = PublishMode.xtfFilesList;
        }
        else{
            if(dbIliIdent_Values == null || dbIliIdent_Values.isEmpty())
                this.publishMode = PublishMode.dbIdentvaluesRegex;
            else
                this.publishMode = PublishMode.dbIdentvaluesList;
        }
    }

    private void assertValidDbArgs() {
        List<String> missingArgs = new ArrayList<>();

        if(dbDatabase == null || dbDatabase.isEmpty())
            missingArgs.add("dbDatabase");

        if(dbSchema == null || dbSchema.isEmpty())
            missingArgs.add("dbSchema");

        if(dbMergeToSingleXtf == null)
            missingArgs.add("dbMergeToSingleXtf");

        if(dbIliIdent_Type == null || dbIliIdent_Type.isEmpty())
            missingArgs.add("dbIliIdent_Type");

        if(!missingArgs.isEmpty()){
            throw new IllegalArgumentException(
                    "Missing mandatory arguments: " + String.join(", ", missingArgs)
            );
        }

        assertDbEitherOr();
        assignIliIdentifierType();
    }

    private void assignIliIdentifierType() {
        IliIdentType identType = null;
        try{
            identType = IliIdentType.valueOf(dbIliIdent_Type);
        }
        catch (Exception e){
            String allowedValues = Arrays.stream(
                    IliIdentType.values()).map(Enum::name).collect(Collectors.joining(", ")
            );

            throw new IllegalArgumentException("dbIliIdent_Type must be one of " + allowedValues);
        }
        this.dbIliIdent_TypeEnum = identType;
    }

    private void assertDbEitherOr() {
        boolean hasRegex = (dbIliIdent_RegEx != null && !dbIliIdent_RegEx.isEmpty());
        boolean hasValueList = (dbIliIdent_Values != null && !dbIliIdent_Values.isEmpty());

        if(hasRegex && hasValueList)
            throw new IllegalArgumentException("Setting both dbIliIdent_RegEx and dbIliIdent_Values is invalid");

        if(!hasRegex && !hasValueList)
            throw new IllegalArgumentException("Either dbIliIdent_RegEx or dbIliIdent_Values must be set");
    }

    private void assertValidXtfArgs() {
        List<String> missingArgs = new ArrayList<>();

        if(xtfFile_FolderPath == null || xtfFile_FolderPath.isEmpty())
            missingArgs.add("xtfFile_FolderPath");

        if(!missingArgs.isEmpty()){
            throw new IllegalArgumentException(
                    "Missing mandatory arguments: " + String.join(", ", missingArgs)
            );
        }

        assertXtfEitherOr();
    }

    private void assertXtfEitherOr() {
        boolean hasRegex = (xtfFilename_Regex != null && !xtfFilename_Regex.isEmpty());
        boolean hasValueList = (xtfFilename_List != null && !xtfFilename_List.isEmpty());

        if(hasRegex && hasValueList)
            throw new IllegalArgumentException("Setting both xtfFilename_Regex and xtfFilename_List is invalid");

        if(!hasRegex && !hasValueList)
            throw new IllegalArgumentException("Either xtfFilename_Regex or xtfFilename_List must be set");
    }

    private void assertAllXtfArgsNull() {
        List<String> errors = new ArrayList<>();

        if(xtfFile_FolderPath != null)
            errors.add("xtfFile_FolderPath");

        if(xtfFilename_List != null)
            errors.add("xtfFilename_List");

        if(xtfFilename_Regex != null)
            errors.add("xtfFilename_Regex");

        if(!errors.isEmpty()){
            throw new IllegalArgumentException(
                    "Publisher is in db mode. These xtf arguments must be null: " + String.join(", ", errors)
            );
        }
    }

    private void assertAllDbArgsNull() {
        List<String> errors = new ArrayList<>();

        if(dbMergeToSingleXtf != null)
            errors.add("dbMergeToSingleXtf");

        if(dbDatabase != null)
            errors.add("dbDatabase");

        if(dbSchema != null)
            errors.add("dbSchema");

        if(dbIliIdent_Type != null)
            errors.add("dbIliIdent_Type");

        if(dbIliIdent_RegEx != null)
            errors.add("dbIliIdent_RegEx");

        if(dbIliIdent_Values != null)
            errors.add("dbIliIdent_Values");

        if(!errors.isEmpty()){
            throw new IllegalArgumentException(
                    "Publisher is in file mode. These db arguments must be null: " + String.join(", ", errors)
            );
        }
    }

    public String getDbDatabase() {
        return dbDatabase;
    }

    public String getDbSchema() {
        return dbSchema;
    }

    public String getDbIliIdent_Type() {
        return dbIliIdent_Type;
    }

    public ArrayList<String> getDbIliIdent_Values() {
        return dbIliIdent_Values;
    }

    public String getDbIliIdent_RegEx() {
        return dbIliIdent_RegEx;
    }

    public Boolean getDbMergeToSingleXtf() {
        return dbMergeToSingleXtf;
    }

    public IliIdentType getDbIliIdentType() {
        return dbIliIdent_TypeEnum;
    }

    public String getXtfFile_FolderPath() {
        return xtfFile_FolderPath;
    }

    public String getXtfFilename_Regex() {
        return xtfFilename_Regex;
    }

    public ArrayList<String> getXtfFilename_List() {
        return xtfFilename_List;
    }

    public PublishMode getPublishMode() {
        return publishMode;
    }

    /*
    Am 6.7 hier weiter Implementieren.

    Anschlussarbeiten:
    - Aus RawPublisherArgs die jeweils relevanten Argumente abhängig von Modus und IliIdent-Setting herausziehen
    - Die Abfolge der Operationen bilden (OpSequenceBuilder)
    - Die Abfolge der Operationen ausführen (OpSequenceRunner)

    Siehe auch Chatverlauf auf ChatGPT

    Irgendwo dazwischen ggf. vertiefte Validierung
    - Regex syntax korrekt
    - Ordner (und Dateien) vorhanden
    - Schema vorhanden?

    public PublishCommand deferCommand(){

    }
    */
}

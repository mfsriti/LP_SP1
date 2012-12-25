package mf.sriti.csp.sp.converter;

import java.io.File;
import java.math.BigInteger;
import java.util.Random;

import mf.sriti.csp.sp.util.FileHandler;
import mf.sriti.csp.sp.util.GeneralException;

public class ConversionFile {
	
	String mTemplateFileName = "";
	String mSATPREFFileName = "";
	String mLPSolveFileName = "";
	
	FileHandler mTemplateFile = null;
	FileHandler mSATPREFFile = null;
	FileHandler mLPSolveFile = null;
	
	int mPercentagePreferences = 0;
	int mNumberOutputFiles = 0;
	int mCounterConstraints = 0;
	int mNumberProblemVariables = 0;
	int mNumberPreferenceVariables = 0;
	
	public ConversionFile(File file) throws ConversionException, GeneralException{	
		ConversionManager cm = ConversionManager.getInstance();
		if (cm==null)
			throw new ConversionException("ConversionFile: ConversionManager singleton is NULL");
		ConversionPropertiesHandler cph = cm.getProps();
		if (cph==null)
			throw new ConversionException("ConversionFile: ConversionPropertiesHandler not initialized yet.");
		
		mTemplateFileName = file.getAbsolutePath();
		String commonName = mTemplateFileName.substring(0,mTemplateFileName.lastIndexOf('.')) + COMMON_FILE_PART_SEP + COMMON_FILE_PART_PREF;
		mSATPREFFileName = commonName.replace(cph.getTemplateDir(), cph.getSATPREFDir());
		mLPSolveFileName = commonName.replace(cph.getTemplateDir(), cph.getLPSolveDir());
		
		mPercentagePreferences = cph.getNbOutput();
		mNumberOutputFiles = cph.getPercentage();
	}
	
/*	private void generateFiles(String originePath, int nbPrefLiterals, int nbOutFile, int percentage) throws ConversionException, GeneralException{
		
		
		int totalFileSize =  (100/percentage) * nbOutFile;
		mSATPREFFiles = new FileHandler[totalFileSize];
		mLPSolveFiles = new FileHandler[totalFileSize];

        for (int i=0; i<totalFileSize; i++){
        		String randStr = COMMON_FILE_PART_PERCENT + COMMON_FILE_PART_SEP + COMMON_FILE_PART_RAND;
        		mSATPREFFiles[i] = new FileHandler(satprefFileName+ (i/nbOutFile+1)*(mProblemVariableNumber*percentage)/100 + COMMON_FILE_PART_SEP + (i/nbOutFile+1)*percentage + randStr + i%nbOutFile + "." + SATPREF_FILE_EXT, false);
        		mLPSolveFiles[i] = new FileHandler(lpsolveFileName+ (i/nbOutFile+1)*(mProblemVariableNumber*percentage)/100 + COMMON_FILE_PART_SEP + (i/nbOutFile+1)*percentage + randStr + i%nbOutFile + "." + LP_SOLVE_FILE_EXT, false);
        }
	}
	
	*/
	public void convert() throws GeneralException {
		System.out.println("ConversionFile.ConversionFile(): processing file: "+ mTemplateFileName);
		extractNumberProblemVarialbles();
		int totalFileSize =  (100/mPercentagePreferences) * mNumberOutputFiles;
        for (int i=0; i<totalFileSize; i++){
        	initConversion(i);
        	String currentLine = null;
			while ((currentLine = mTemplateFile.nextLine()) != null ) {
				processLine(currentLine);
			}
			finalizeConversion();	
		}
	
	}
	
	private void initConversion(int index) throws GeneralException {
		String randStr = COMMON_FILE_PART_PERCENT + COMMON_FILE_PART_SEP + COMMON_FILE_PART_RAND;
    	mNumberPreferenceVariables = (index/mNumberOutputFiles+1)*((mNumberProblemVariables*mPercentagePreferences)/100);
    	mSATPREFFile = new FileHandler(mSATPREFFileName+ mNumberPreferenceVariables + COMMON_FILE_PART_SEP + (index/mNumberOutputFiles+1)*mPercentagePreferences + randStr + index%mNumberOutputFiles + "." + SATPREF_FILE_EXT, false);
    	mLPSolveFile = new FileHandler(mLPSolveFileName+ mNumberPreferenceVariables + COMMON_FILE_PART_SEP + (index/mNumberOutputFiles+1)*mPercentagePreferences + randStr + index%mNumberOutputFiles + "." + LP_SOLVE_FILE_EXT, false);
    	
    	mTemplateFile = new FileHandler(mTemplateFileName, true);
		mCounterConstraints = 0;
	}
	
	private void finalizeConversion() throws GeneralException {
		mTemplateFile.close();
		mSATPREFFile.close();
		mLPSolveFile.close();
	}

	private void extractNumberProblemVarialbles() throws GeneralException {
		mTemplateFile = new FileHandler(mTemplateFileName, true);
		mTemplateFile.nextLine();
		String a[]=mTemplateFile.nextLine().split(" ");
		mNumberProblemVariables = Integer.parseInt(a[2]);
		mTemplateFile.close();
	}
	private void processLine(String line) throws ConversionException, GeneralException{
		// for the SATPREF file, we print the following line as they are:
		//  -contains only "c"  -equals to "c preferences"     
		if ( line.equals("c") || line.equals("c preferences") ){
			writeToSATPREF(line);
			// for lpsolve, nothing to do
		} else if (line.isEmpty()){
			writeToSATPREF(line);
			writeToLPSolve("");
			writeToLPSolve(LP_SOLVE_TERM_BIN);
			writeToLPSolve(generateBinarySection());
		} else{
			switch(line.charAt(0)){
			case 'c':
				if (line.startsWith("c S")) {//we process the line contains variable counters, ex: "c S 200 50"
					writeToSATPREF(line + " "+ mNumberPreferenceVariables);
					writeToLPSolve(LP_SOLVE_TERM_MAX+":");
				} else { // here it remains only the list of pref line: "c 84<-29, 135<-29, -169<-185, 82<16, ..."
					if (mNumberProblemVariables<=0 || mNumberPreferenceVariables<=0)
						throw new ConversionException("CobnversionFile.processLine: either problemVariableNumber or prefsLiteralNumber is not set properly");
					// generate randomly prefs literals from problem variables
					int prefsLiterals[] = generatePrefsLiterals(mNumberProblemVariables, mNumberPreferenceVariables);
					
					// generate prefs
					String prefsString = generatePreferences(prefsLiterals);
					// write the prefs string into the file
					writeToSATPREF(prefsString);
					// generate objective function
					String objFunction = generateObjFunction(prefsLiterals);
					writeToLPSolve(objFunction);
					writeToLPSolve("");
				}
				break;
			case 'p': // line.startsWith("p cnf")
				writeToSATPREF(line);
			    break;
			case '-':
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				writeToSATPREF(line);
				
				String strConstraint[] = line.split(" ");
				int intConstraint[] = new int [strConstraint.length-1];
				for (int i=0; i<strConstraint.length-1; i++)
					intConstraint[i] = Integer.parseInt(strConstraint[i]);
				
				String constraint = generateConstraint(intConstraint);
				writeToLPSolve(LP_SOLVE_TERM_CONSTRAINT+ ++mCounterConstraints + ":  "+constraint);
				break;
			default: ;
				
			}
		}
	}
	
	private void writeToFile(FileHandler file, String line)  throws GeneralException {
		file.writeLine(line);
	}
	
	private void writeToSATPREF(String line) throws GeneralException {
			writeToFile(mSATPREFFile, line);
	}
	
	private void writeToLPSolve(String line) throws GeneralException{
			writeToFile(mLPSolveFile, line);
	}
	
	private int[] generatePrefsLiterals(int nbVariables, int nbLiterals){
		int variables [] = new int[nbVariables];
		int literals [] = new int[nbLiterals];
		
		for(int i=0; i<nbVariables; i++)
			variables [i]=i+1;
		
		int nbRemainders = nbVariables;
		Random random = new Random();
		for (int i=0; i<nbLiterals; i++){
				int lit = random.nextInt(nbRemainders--);
				literals [i] = variables[lit] * (random.nextBoolean() ? 1 : -1);
				variables[lit] = variables[nbRemainders];
		}
		return literals;
	}
	
	private String generatePreferences(int literals[]){
		String result = "c ";
		for(int i=0; i<literals.length-1; i++){
			result += literals[i] + "<" + literals[i+1] + ", "; 
		}
		result = result.substring(0, result.length() - 2);
		result +=";";
		return result;
	}
	
	private String generateObjFunction(int literals[]){
		String result = "";
		for(int i=literals.length-1; i>=0;i--){
			BigInteger bigint = BigInteger.valueOf(2);
			char sign = '+';
			if (literals[i]<0){
				sign = '-';
				literals[i]*=-1;
			}
			result = " " + sign + " " + bigint.pow((literals.length-1) -i).toString() 
					+ "x" + literals[i] + result;
		}
		if (result.startsWith(" + "))
			result = result.substring(3, result.length());
		else // if (result.startsWith(" - "))
			result = "-" + result.substring(3, result.length());
		
		result +=";";
		return result;
	}
	
	private String generateConstraint(int constraint[] ){
		String result = "";
		String space="";
		int negatives = 0;
		
		for (int i=0; i<constraint.length;i++){
			char sign = '+';
			if (constraint[i]<0){
				sign = '-';
				++negatives;
				constraint[i]*=-1;
			}
			result += space + sign + space +"x" + constraint[i] ;
			space=" ";
		}
		
		if (result.startsWith("+"))
			result = result.substring(1, result.length());
		result += " >= " + (1 - negatives);
		
		result +=";";
		return result;
	}
		
	private String generateBinarySection(){
		String result = "";
		for (int i=1; i<=mNumberProblemVariables; i++)
			result += " x" + i +", ";
		result = result.substring(0, result.length() - 2);
		result +=";";
		return result;
	}

	private final static String LP_SOLVE_TERM_MAX = "max";
	private final static String LP_SOLVE_TERM_BIN = "bin";
	private final static String LP_SOLVE_TERM_CONSTRAINT = "C";
	private final static String LP_SOLVE_FILE_EXT = "lp";
	private final static String SATPREF_FILE_EXT = "noptsat";	
	private final static String COMMON_FILE_PART_PREF = "pref";
	private final static String COMMON_FILE_PART_RAND = "rand";
	private final static String COMMON_FILE_PART_PERCENT = "%";
	private final static String COMMON_FILE_PART_SEP = "-";
}

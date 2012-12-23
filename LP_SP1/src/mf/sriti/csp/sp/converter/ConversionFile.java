package mf.sriti.csp.sp.converter;

import java.io.File;
import java.math.BigInteger;
import java.util.Random;

import mf.sriti.csp.sp.util.FileHandler;
import mf.sriti.csp.sp.util.GeneralException;

public class ConversionFile {
	FileHandler mTemplateFile = null;
	FileHandler mSATPREFFiles[] = null;
	FileHandler mLPSolveFiles[] = null;
	
	String mProblemName = "";

	int mProblemVariableNumber = 0;
	int mPrefsLiteralNumber = 0;
	int mConstraintsCounter = 0;
	public ConversionFile(File file) throws ConversionException, GeneralException{
		this(file, 0, 1, 100);
	}
	public ConversionFile(File file, int nbPrefLiterals, int nbOutFile, int percentage) throws ConversionException, GeneralException{
		ConversionManager cm = ConversionManager.getInstance();
		if (cm==null)
			throw new ConversionException("ConversionFile: ConversionManager singleton is NULL");
		
		ConversionPropertiesHandler cph = cm.getProps();
		if (cph==null)
			throw new ConversionException("ConversionFile: ConversionPropertiesHandler not initialized yet.");
		
		mPrefsLiteralNumber = nbPrefLiterals;
		mTemplateFile = new FileHandler(file, true);
		mProblemName = file.getName();
		String templateName = file.getAbsolutePath();
		String satprefFileName = templateName.replace(cph.getTemplateDir(), cph.getSATPREFDir());
		String lpsolveFileName = templateName.replace(cph.getTemplateDir(), cph.getLPSolveDir());
		// change file extensions
		// gen-200var-800cl-pb0-total-pref20-ran0
		String commonPartFileName = COMMON_FILE_PART_SEP + COMMON_FILE_PART_PREF;
		satprefFileName = satprefFileName.substring(0,satprefFileName.lastIndexOf('.')) + commonPartFileName;
		lpsolveFileName = lpsolveFileName.substring(0,lpsolveFileName.lastIndexOf('.')) + commonPartFileName;
		
		int totalFileSize =  (100/percentage) * nbOutFile;
		mSATPREFFiles = new FileHandler[totalFileSize];
		mLPSolveFiles = new FileHandler[totalFileSize];

        for (int i=0; i<totalFileSize; i++){
        		String randStr = COMMON_FILE_PART_PERCENT + COMMON_FILE_PART_SEP + COMMON_FILE_PART_RAND;
        		mSATPREFFiles[i] = new FileHandler(satprefFileName+ (i/nbOutFile+1)*(nbPrefLiterals*percentage)/100 + COMMON_FILE_PART_SEP + (i/nbOutFile+1)*percentage + randStr + i%nbOutFile + "." + SATPREF_FILE_EXT, false);
        		mLPSolveFiles[i] = new FileHandler(lpsolveFileName+ (i/nbOutFile+1)*(nbPrefLiterals*percentage)/100 + COMMON_FILE_PART_SEP + (i/nbOutFile+1)*percentage + randStr + i%nbOutFile + "." + LP_SOLVE_FILE_EXT, false);
        }
	  
	}
	
	
	public void convert() throws GeneralException{
		String currentLine = null;
		System.out.println("ConversionFile.ConversionFile(): processing file: "+ mProblemName);
		while ((currentLine = mTemplateFile.nextLine()) != null ) {
			processLine(currentLine);
		}
		mTemplateFile.close();
		for (int i=0; i<mSATPREFFiles.length; i++){
			mSATPREFFiles[i].close();
			mLPSolveFiles[i].close();
		}
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
			writeToLPSolve(generateIntegersSection());
		} else{
			switch(line.charAt(0)){
			case 'c':
				if (line.startsWith("c S")) {//we process the line contains variable counters, ex: "c S 200 50"
					String a[]=line.split(" ");
					mProblemVariableNumber = Integer.parseInt(a[2]);
					mPrefsLiteralNumber = Integer.parseInt(a[3]);
					writeToSATPREF(line);
					writeToLPSolve(LP_SOLVE_TERM_MAX+":");
				} else { // here it remains only the list of pref line: "c 84<-29, 135<-29, -169<-185, 82<16, ..."
					if (mProblemVariableNumber<=0 || mPrefsLiteralNumber<=0)
						throw new ConversionException("CobnversionFile.processLine: either problemVariableNumber or prefsLiteralNumber is not set properly");
					// generate randomly prefs literals from problem variables
					int prefsLiterals[] = generatePrefsLiterals(mProblemVariableNumber, mPrefsLiteralNumber);
					
					// generate prefs
					String prefsString = generatePreferences(prefsLiterals);
					// write the prefs string into the file
					writeToSATPREF(prefsString);
					// generate objective function
					String objFunction = generateObjFunction(prefsLiterals);
					writeToLPSolve( objFunction);
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
				
				String constraint = generateConstraints(intConstraint);
				writeToLPSolve(LP_SOLVE_TERM_CONSTRAINT+ ++mConstraintsCounter + ":  "+constraint);
				break;
			default: ;
				
			}
		}
	}
	
	private void writeToFile(String line, FileHandler file)  throws GeneralException {
		file.writeLine(line);
	}
	
	private void writeToSATPREF(String line) throws GeneralException {
		for (int i=0; i<mSATPREFFiles.length; i++)
			writeToFile(line, mSATPREFFiles[i]);
	}
	
	private void writeToLPSolve(String line) throws GeneralException{
		for (int i=0; i<mLPSolveFiles.length; i++){
			writeToFile(line, mLPSolveFiles[i]);
		}
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
			
		return result;
	}
	
	private String generateConstraints(int constraint[] ){
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
		result += ">=" + (1 - negatives);
		return result;
	}
		
	private String generateIntegersSection(){
		String result = "";
		for (int i=1; i<=mProblemVariableNumber; i++)
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

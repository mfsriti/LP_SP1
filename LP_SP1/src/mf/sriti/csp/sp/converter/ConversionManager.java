package mf.sriti.csp.sp.converter;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import mf.sriti.csp.sp.util.FileHandler;
import mf.sriti.csp.sp.util.GeneralException;

public class ConversionManager {
	static ConversionManager sInstance=null;
	ConversionPropertiesHandler mProps;
	
	public static ConversionManager getInstance() throws GeneralException{
		if (sInstance==null)
			sInstance = new ConversionManager(); 
		return sInstance;
	}
	
	private ConversionManager() throws GeneralException {
		mProps = new ConversionPropertiesHandler();
		System.out.println("Source directory: "+mProps.getSourceDir());
		System.out.println("Target_Old directory: "+mProps.getTargetOldDir());
		System.out.println("Target_New directory: "+mProps.getTargetNewDir());
	}
	public void process(){
		//1- parcourir les sous dossier source
		try {
			List<File> files = new FileHandler(mProps.getSourceDir()).listChildren(true);
			Iterator<File> it = files.iterator();
			while(it.hasNext()){
				ConversionFile cf = new ConversionFile((File)it.next());
				cf.convert();
			}
			
		
		} catch(GeneralException ge){
			System.out.println("ConversionManager.process: Error when accessing source dir: "+ ge.getMessage());
		}
		//2- pour chaque fichier, convertir dans les deux formats
		//3- enregistrer les deux nouveaux fichiers dans leurs dossier respectiive en respectant la meme structure 

		
		// 2- for each source file, convert it and put the target files to the same dir in the target dir
	}
	public ConversionPropertiesHandler getProps(){
		return mProps;
	}
}


import java.io.File;
import javax.swing.filechooser.*;

public class AccessFileFilter extends FileFilter {
    public String fileExt = "";
    String accessExt = ".mdb";

    public AccessFileFilter() {
        this(".mdb");  //default file type extension.
    }

    public AccessFileFilter(String extension) {
        fileExt = extension;
    }

     @Override public boolean accept(File f) {
        if (f.isDirectory())
            return true;
        return  (f.getName().toLowerCase().endsWith(fileExt)); 
    }

    public String getDescription() {
        if(fileExt.equals(accessExt))
            return  "Access Files (*" + fileExt + ")";
        else
            return ("Other File");
    }
}

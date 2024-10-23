import java.util.ArrayList;

public class GPXFile {
    String file_name;
    int chunks_num;
    ArrayList<IntermediateResults> inter_res;

    public GPXFile(String file_name) {
        this.file_name = file_name;
        this.chunks_num = 0;
        inter_res = new ArrayList<>();
    }

    public String getFile_name() {
        return file_name;
    }

    public int getChunks_num() {
        return chunks_num;
    }

    public void setChunks_num(int chunks_num) {
        this.chunks_num = chunks_num;
    }

    public ArrayList<IntermediateResults> getInter_res() {
        return inter_res;
    }

}

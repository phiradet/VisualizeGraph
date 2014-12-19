/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visualizegraph;

import GexfToVectorImage.GexfToVectorImage;
import org.openide.util.Exceptions;

/**
 *
 * @author Phiradet Bangcharoensap
 */
public class VisualizeGraph {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //args = new String[] {"force","png","./data/clique.gexf","./data/clique-fromFile-expand.png"};
        if(args.length!=4)
        {
            System.out.println("java -jar VisualizaGraph.java <yifan|fruch|openord|force> <pdf|png|svg> <input GEXF> <output SVG>");
            return;
        }
        GexfToVectorImage g = new GexfToVectorImage();
        
        int layout = -1;
        if(args[0].toLowerCase().equals("yifan"))
        {
            layout = GexfToVectorImage.LAYOUT_YIFANHU;
        }
        else if(args[0].toLowerCase().equals("fruch"))
        {
            layout = GexfToVectorImage.LAYOUT_FRUCHTERMAN;
        }
        else if(args[0].toLowerCase().equals("openord"))
        {
            layout = GexfToVectorImage.LAYOUT_OPENORD;
        }
        else if(args[0].toLowerCase().equals("force"))
        {
            layout = GexfToVectorImage.LAYOUT_FORCE_ATLAS;
        }
        
        int outputType = -1;
        if(args[1].toLowerCase().equals("pdf"))
        {
            outputType = GexfToVectorImage.OUTPUT_PDF;
        }
        else if(args[1].toLowerCase().equals("svg"))
        {
            outputType = GexfToVectorImage.OUTPUT_SVG;
        }
        else if(args[1].toLowerCase().equals("png"))
        {
            outputType = GexfToVectorImage.OUTPUT_PNG;
        }
        String gexfFile = args[2];
        String svgFile = args[3];
        //String gexfFile = "./data/clique.gexf";
        //String svgFile = "./data/cliqeu.svg";
        System.out.format("Input:%s\nOutput:%s\n", gexfFile, svgFile);
        try {
            System.out.format("Executing %d %d\n", outputType, layout);
            g.export(gexfFile, svgFile, outputType, layout);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
}

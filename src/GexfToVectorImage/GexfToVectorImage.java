/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package GexfToVectorImage;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.preview.PDFExporter;
import org.gephi.io.exporter.preview.PNGExporter;
import org.gephi.io.exporter.preview.SVGExporter;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.AutoLayout;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.layout.plugin.forceAtlas.ForceAtlasLayout;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2Builder;
import org.gephi.layout.plugin.fruchterman.FruchtermanReingold;
import org.gephi.layout.plugin.fruchterman.FruchtermanReingoldBuilder;
import org.gephi.layout.plugin.multilevel.YifanHuMultiLevel;
import org.gephi.layout.plugin.openord.OpenOrdLayout;
import org.gephi.layout.plugin.openord.OpenOrdLayoutBuilder;
import org.gephi.layout.plugin.scale.Expand;
import org.gephi.layout.plugin.scale.ScaleLayout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.types.EdgeColor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author PHIRADET
 */
public class GexfToVectorImage {
    
    public final static int OUTPUT_PDF = 10;
    public final static  int OUTPUT_SVG = 11;
    public final static int OUTPUT_PNG = 12;
    
    public final static  int LAYOUT_YIFANHU = 20;
    public final static int LAYOUT_FRUCHTERMAN = 21;
    public final static int LAYOUT_OPENORD = 22;
    public final static int LAYOUT_FORCE_ATLAS = 23;
    public final static int LAYOUT_EXPAND = 24;
    
    private ProjectController pc;
    private Workspace workspace;
    
    public GexfToVectorImage() {
        pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        workspace = pc.getCurrentWorkspace();
    }
    
    private void importFile(String filename)
    {
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        Container container = null;
        
        try{
            System.out.println("Start reading gexf file");
            File file = new File(filename);
            System.out.println("Finishing reading gexf file");
            container = importController.importFile(file);
            container.getLoader().setEdgeDefault(EdgeDefault.UNDIRECTED);
            container.setAllowAutoNode(false);
        }
        catch (Exception ex)
        {
            System.out.println("Error during reading the file:");
            ex.printStackTrace();
        }
        importController.process(container, new DefaultProcessor(), workspace);
    }
    
    private void applyLayoutFromFile(String positionFile, DirectedGraph graph) throws IOException
    {
        Map<String, Float> Xref = new HashMap<String, Float>();
        Map<String, Float> Yref = new HashMap<String, Float>();
        Map<String, Float> Zref = new HashMap<String, Float>();
        
        File f = new File(positionFile);
        BufferedReader reader = new BufferedReader(new FileReader(f));
        String line = null;
        while((line = reader.readLine())!=null)
        {
            String[] splString = line.split(",");
            String node = splString[0];
            float x = Float.parseFloat(splString[1]);
            float y = Float.parseFloat(splString[2]);
            float z = Float.parseFloat(splString[3]);
            Xref.put(node, x);
            Yref.put(node, y);
            Zref.put(node, z);
        }
        
        for(Node n:graph.getNodes())
        {
            String currNodeName = n.getNodeData().getLabel();
            float currX = Xref.get(currNodeName);
            float currY = Yref.get(currNodeName);
            float currZ = Zref.get(currNodeName);
            n.getNodeData().setX(currX);
            n.getNodeData().setY(currY);
            n.getNodeData().setZ(currZ);
        }
    }
    
    private void applyLayout(GraphModel graphModel, int layoutType, String locationFile) throws InterruptedException
    {
        //Layout
        System.out.println("Executing layout");
        if(layoutType == LAYOUT_FRUCHTERMAN)
        {
            FruchtermanReingold layout = new FruchtermanReingold(new FruchtermanReingoldBuilder());
            AutoLayout autoLayout = new AutoLayout(10, TimeUnit.SECONDS);
            autoLayout.setGraphModel(graphModel);
            autoLayout.addLayout(layout, 1.0f);
            System.out.println("\tStart FRUCHTHERMAN Layout >>");
            autoLayout.execute();
        }
        else if(layoutType == LAYOUT_YIFANHU)
        {
            YifanHuLayout layout = new YifanHuLayout( new YifanHuMultiLevel(), new StepDisplacement(1f));
            AutoLayout autoLayout = new AutoLayout(10, TimeUnit.SECONDS);
            autoLayout.setGraphModel(graphModel);
            autoLayout.addLayout(layout, 1.0f);
            System.out.println("\tStart YIFANHU Layout >>");
            autoLayout.execute();
        }
        else if(layoutType == LAYOUT_OPENORD)
        {
            LayoutBuilder builder = new OpenOrdLayoutBuilder();
            OpenOrdLayout layout = new OpenOrdLayout(builder);
            layout.setGraphModel(graphModel);
            layout.resetPropertiesValues();
            //layout.setExpansionStage(40);
            //layout.setNumIterations(750);
            layout.setEdgeCut(0.2f);
            //====== Layout parameter ======
            System.out.println("\tCooldown:"+layout.getCooldownStage());
            System.out.println("\tCrunch:"+layout.getCrunchStage());
            System.out.println("\tEdge-cut:"+layout.getEdgeCut());
            System.out.println("\tExpansion:"+layout.getExpansionStage());
            System.out.println("\tLiquid:"+layout.getLiquidStage());
            System.out.println("\tSimmer:"+layout.getSimmerStage());
            System.out.println("\tIteration:"+layout.getNumIterations());
            layout.initAlgo();
            System.out.println("\tStart OPENORD Layout >>");
            //layout.goAlgo();
            while(layout.canAlgo())
                layout.goAlgo();
            //autoLayout.addLayout(layout, 1.0f);
        }
        else if(layoutType == LAYOUT_FORCE_ATLAS)
        {
            ForceAtlas2Builder builder = new ForceAtlas2Builder();
            ForceAtlasLayout layout = new ForceAtlasLayout(builder);
            layout.setGraphModel(graphModel);
            layout.resetPropertiesValues();
            layout.initAlgo();
            while(layout.canAlgo())
                layout.goAlgo();
        }
        else if(layoutType == LAYOUT_EXPAND)
        {
            Expand builder = new Expand();
            ScaleLayout layout = new ScaleLayout(builder, 2f);
            layout.setGraphModel(graphModel);
            layout.resetPropertiesValues();
            layout.initAlgo();
             while(layout.canAlgo())
                layout.goAlgo();
        }
        System.out.println("Finish executing layout");
        
        System.out.println("Recording layout info to file");
        recordLayout(locationFile, graphModel.getDirectedGraph());
        System.out.println("Finish recording layout");
    }
    
    public void recordLayout(String outputFile, DirectedGraph graph)
    {
        try {
            FileWriter fileWrite = new FileWriter(outputFile);
            PrintWriter printWrite = new PrintWriter(fileWrite);
            for(Node n: graph.getNodes())
            {
                String nodeName = n.getNodeData().getLabel();
                float X = n.getNodeData().x();
                float Y = n.getNodeData().y();
                float Z = n.getNodeData().z();
                printWrite.printf("%s,%f,%f,%f\n", nodeName, X, Y, Z);
            }
            printWrite.close();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
    }
    
    public void export(String gexfFile, String outputFile,int outputType, int layoutType, String locationFile) throws Exception
    {
        if(outputType != OUTPUT_PDF && outputType != OUTPUT_SVG && outputType != OUTPUT_PNG)
        {
            throw new Exception("Only support PDF or SVG format: "+outputType);
        }
        
        /*if(layoutType != LAYOUT_YIFANHU && layoutType != LAYOUT_FRUCHTERMAN)
        {
            throw new Exception("Only support Fruchterman Reingold or Yifan Hu layout");
        }*/
        
        importFile(gexfFile);
        
        //See if graph is well imported
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        DirectedGraph graph = graphModel.getDirectedGraph();
        System.out.println("Nodes: " + graph.getNodeCount());
        System.out.println("Edges: " + graph.getEdgeCount());
        
        //Edit node's color according to a attribute
        //int countFraudNode = 0;
        /*
        for(Node n: graph.getNodes())
        {
            String type = n.getAttributes().getValue("type").toString();
            if(type.equals("bidder"))
                n.getNodeData().setColor(0f, 0f, 1f);
            else if(type.equals("seller"))
                n.getNodeData().setColor(1f, 0f, 0f);
            else if(type.equals("both"))
                n.getNodeData().setColor(1.0f, 0.78f, 0f);
            
            if(n.getAttributes().getValue("fraud") != null)
            {
                int isFraud = Integer.parseInt(n.getAttributes().getValue("fraud").toString());
                if(isFraud == 1)
                {
                    n.getNodeData().setColor(0.8f, 0f, 0.8f);
                    n.getNodeData().setSize(20f);
                    countFraudNode+=1;
                }
                //else
                n.getNodeData().setSize(5f);
            }
        }
        System.out.format("#of Fraudsters:%d\n",countFraudNode);
        */
        System.out.println("Applying layout");
        applyLayout(graphModel, layoutType, locationFile);
        //applyLayoutFromFile(locationFile, graph);
        //applyLayout(graphModel, LAYOUT_EXPAND);
        System.out.println("Finished applying layout");
        
        //Get preview object
        PreviewModel model = Lookup.getDefault().lookup(PreviewController.class).getModel();
        PreviewProperties prop = model.getProperties();
        prop.putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.FALSE);
        prop.putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(Color.WHITE));
        prop.putValue(PreviewProperty.NODE_BORDER_WIDTH, "parent");
        prop.putValue(PreviewProperty.EDGE_THICKNESS, 0.2f);
        prop.putValue(PreviewProperty.EDGE_CURVED, Boolean.FALSE);
        prop.putValue(PreviewProperty.DIRECTED, Boolean.TRUE);
        prop.putValue(PreviewProperty.EDGE_RESCALE_WEIGHT, Boolean.TRUE);
        prop.putValue(PreviewProperty.EDGE_OPACITY, 100);
        
        System.out.println("Start printing");
        //Export
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        if(outputType==OUTPUT_PDF)
        {
            try{
                ec.exportFile(new File(outputFile));
            }catch (IOException ex)
            {
                ex.printStackTrace();
            }            
            PDFExporter pdfExporter = (PDFExporter) ec.getExporter("pdf");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ec.exportStream(baos, pdfExporter);
        }
        else if(outputType==OUTPUT_SVG)
        {
            SVGExporter svgExporter = (SVGExporter)ec.getExporter("svg");
            svgExporter.setWorkspace(workspace);
            
            try {
                svgExporter.setWriter(new FileWriter(outputFile));
                svgExporter.execute();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        else if(outputType==OUTPUT_PNG)
        {
            try{
                ec.exportFile(new File(outputFile));
            }catch (IOException ex)
            {
                ex.printStackTrace();
            }            
            PNGExporter pngExporter = (PNGExporter)ec.getExporter("png");
            pngExporter.setHeight(10000);
            pngExporter.setWidth(10000);
            pngExporter.setWorkspace(workspace);
            
            OutputStream outputStream = new FileOutputStream(outputFile);
            pngExporter.setOutputStream(outputStream);
            pngExporter.execute();
            //ec.exportStream(baos, pngExporter);
            System.out.format("H: %d, W:%d\n", pngExporter.getHeight(), pngExporter.getWidth());
        }
        System.out.println("Finished printing");
    }
}

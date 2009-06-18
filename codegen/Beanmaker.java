import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.io.IOException;

public class Beanmaker {

  private Beanmaker() {
    // Can't construct me
  }

  public static final void main(String args[]) {

    // Check we have sensible number of arguments
    if (args==null||args.length < 3) {
      usage();
      System.exit(1);
    }

    // Argument counter
    int argIdx=0;

    boolean writePackage=false;

    // -p switch indicates we should write out package statement
    if (args[argIdx].equals("-p")) {
      writePackage=true;
      argIdx++;
    }

    // Next argument is filename
    String fileName=args[argIdx];
    argIdx++;
    if (fileName.endsWith(".java")) {
      fileName=fileName.substring(0,fileName.length()-5);
    }

    // Replace slashes in path with dots
    String dottedName=fileName.replace('/','.');
    dottedName=dottedName.replace('\\','.');

    int lastDotIdx=dottedName.lastIndexOf(".");

    // Figure out package and class to generate from filename
    String packageName;
    String className;
    if (lastDotIdx<0) {
      packageName="";
      className=dottedName;
    } else {
      packageName=dottedName.substring(0,lastDotIdx);
      className=dottedName.substring(lastDotIdx+1);
    }

    // Check we have even number of args left
    if ((args.length - argIdx)%2 != 0) {
      usage();
      System.exit(1);
    }

    // Build array of property type/name pairs
    int numProperties=(args.length - argIdx)/2;
    String properties[][]=new String[numProperties][2];

    for (int I=0;I<numProperties;I++,argIdx+=2) {
      properties[I][0]=args[argIdx];
      properties[I][1]=args[argIdx+1];
    }

    try {

      // Create file and any parent dirs needed
      File classFile=new File(fileName+".java");
      if (classFile.getParentFile() != null) 
        classFile.getParentFile().mkdirs();

      // Begin writing to the file
      Writer out=new FileWriter(classFile);

      // Write package statement if necessary
      if (writePackage) {
        out.write("package "+packageName+";\n");
        out.write("\n");
      }

      // Write class declaration
      out.write("public class "+className+" implements java.io.Serializable {\n");
      out.write("\n");
      // 
      // Generate declarations for each property
      for (int I=0;I<properties.length;I++) {
        generateDeclaration(properties[I][0],properties[I][1],out);
      }

      out.write("\n");
      
      // Generate default constructor
      //out.write("  /**\n");
      //out.write("   * Creates a new instance of "+className+"\n");
      //out.write("   */\n");
      //out.write("  public "+className+"() {\n");
      //out.write("  }\n");
      //out.write("\n");


      // Generate getters and setters
      for (int I=0;I<properties.length;I++) {
        generateAccessor(properties[I][0],properties[I][1],out);
        generateMutator(properties[I][0],properties[I][1],out);
      }

      out.write("}");
      out.flush();
      out.close();
      System.exit(0);
    } catch (IOException ex) {
      System.out.println("Couldn't create source file");
      ex.printStackTrace();
    }
  }

  static void generateDeclaration(String type, String name, Writer out) throws IOException {
    out.write("  private "+type+" "+name+";\n");
  }

  static void generateAccessor(String type, String name, Writer out) throws IOException {

    String methodName;
    if (type.equals("boolean")) {
      methodName="is"+capitalise(name);
    } else {
      methodName="get"+capitalise(name);
    }

    //out.write("  /**\n");
    //out.write("   * Gets the current value of "+name+"\n");
    //out.write("   * @return Current value of "+name+"\n");
    //out.write("   */\n");
    out.write("  public "+type+" "+methodName+"() {\n");
    out.write("    return "+name+";\n");
    out.write("  }\n");
    //out.write("\n");
  }

  static void generateMutator(String type, String name, Writer out) throws IOException {

    String methodName="set"+capitalise(name);

    //out.write("  /**\n");
    //out.write("   * Sets the value of "+name+"\n");
    //out.write("   * @param "+name+" New value for "+name+"\n");
    //out.write("   */\n");
    out.write("  public void "+methodName+"("+type+" "+name+") {\n");
    out.write("    this."+name+"="+name+";\n");
    out.write("  }\n");
    out.write("\n");

  }

  /**
   * Capitalises first character of input
   * @param name String to capitalise
   * @return Input with first character uppercased
   */
  private static String capitalise(String name) {
    return name.substring(0,1).toUpperCase()+name.substring(1);
  }

  private static void usage() {

    System.out.println("  Beanmaker [-p] <filename> <type name> ...");

    System.out.println("  filename - sourcefile is written here, classname inferred from filename");
    System.out.println("  usage: java Beanmaker -p com/chimpen/apes/Monkey String name boolean newWorld int nuts");
    System.out.println("  -p Generates a package statement from the filename");
  }
}

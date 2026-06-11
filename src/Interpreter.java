import AST.*;
import java.util.HashMap;
import java.util.LinkedList;

public class Interpreter
{
    // Store AST reference for later use
    private Nusha astTree;
    
    // Definitions: maps definition name to array of options
    private HashMap<String, String[]> definitions;
    
    // Variables: maps variable name to array of variable instances
    private HashMap<String, VariableInstance[]> variables;
    
    // Structures: maps structure name to array of structure instances
    private HashMap<String, StructInstance[]> structures;
    
    /**
     * VariableInstance represents a single variable instance.
     * Uses integer index (not String) for efficiency.
     */
    private class VariableInstance
    {
        String definitionName;  // Reference to which definition this uses
        int currentValue;       // Index into the definition's String[] (required for efficiency)
        VariableInstance[] uniqueWith;  // Other variables this must be unique with
        
        VariableInstance(String definitionName)
        {
            this.definitionName = definitionName;
            this.currentValue = 0;  // Start with first option
            this.uniqueWith = null; // Will be set later if needed
        }
    }
    
    /**
     * StructInstance represents a single structure instance.
     * Contains HashMap of member names to their variable instances.
     */
    private class StructInstance
    {
        HashMap<String, VariableInstance> members;
        
        StructInstance()
        {
            this.members = new HashMap<>();
        }
    }
    
    public void Interpret(Nusha tree) throws Exception
    {
        this.astTree = tree;
        definitions = new HashMap<>();
        variables = new HashMap<>();
        structures = new HashMap<>();
        
        // Process definitions
        processDefinitions(tree.definitions);
        
        // Process variables
        processVariables(tree.variables);
        
        // Set up unique constraints
        setupUniqueConstraints(tree.variables);
        
        // Print all variables for verification
        printAllVariables();
    }
    
    /**
     * Process all definitions from AST and store in HashMap.
     * Definitions are stored as String[] for fast lookup.
     */
    private void processDefinitions(Definitions defs) throws Exception
    {
        if (defs == null || defs.definition == null)
        {
            return;
        }
        
        for (Definition def : defs.definition)
        {
            String name = def.definitionName;
            
            // Handle simple definitions (choices like Boy = {Tom, Fred, Barney})
            if (def.choices.isPresent())
            {
                Choices choices = def.choices.get();
                String[] options = new String[choices.choice.size()];
                int i = 0;
                for (String choice : choices.choice)
                {
                    options[i++] = choice;
                }
                definitions.put(name, options);
            }
            // Struct definitions are handled when creating variables
        }
    }
    
    /**
     * Process all variables from AST.
     * Creates either simple variable arrays or structure arrays.
     */
    private void processVariables(Variables vars) throws Exception
    {
        if (vars == null || vars.variable == null)
        {
            return;
        }
        
        for (Variable var : vars.variable)
        {
            String varName = var.variableName;
            String varType = var.type;
            int arraySize = 1;  // Default to single instance
            
            // Parse array size if present
            if (var.size.isPresent())
            {
                arraySize = Integer.parseInt(var.size.get());
            }
            
            // Check if type is a simple definition or a struct
            if (definitions.containsKey(varType))
            {
                // Simple variable (references a definition with choices)
                VariableInstance[] varArray = new VariableInstance[arraySize];
                for (int i = 0; i < arraySize; i++)
                {
                    varArray[i] = new VariableInstance(varType);
                }
                variables.put(varName, varArray);
            }
            else
            {
                // Struct type - create struct instances
                StructInstance[] structArray = new StructInstance[arraySize];
                for (int i = 0; i < arraySize; i++)
                {
                    structArray[i] = createStructInstance(varType);
                }
                structures.put(varName, structArray);
            }
        }
    }
    
    /**
     * Create a struct instance based on struct definition from AST.
     */
    private StructInstance createStructInstance(String structTypeName) throws Exception
    {
        StructInstance instance = new StructInstance();
        
        // Find the struct definition
        NStruct structDef = findStructDefinition(structTypeName);
        if (structDef == null)
        {
            throw new Exception("Struct definition not found: " + structTypeName);
        }
        
        // Create a variable instance for each entry in the struct
        for (Entry entry : structDef.entry)
        {
            VariableInstance varInst = new VariableInstance(entry.type);
            instance.members.put(entry.name, varInst);
        }
        
        return instance;
    }
    
    /**
     * Find a struct definition by name in the AST.
     */
    private NStruct findStructDefinition(String name)
    {
        if (astTree.definitions == null || astTree.definitions.definition == null)
        {
            return null;
        }
        
        for (Definition def : astTree.definitions.definition)
        {
            if (def.definitionName.equals(name) && def.nstruct.isPresent())
            {
                return def.nstruct.get();
            }
        }
        return null;
    }
    
    /**
     * Set up unique constraints between variables in struct arrays.
     * Each unique variable must track all other instances it must differ from.
     */
    private void setupUniqueConstraints(Variables vars) throws Exception
    {
        if (vars == null || vars.variable == null)
        {
            return;
        }
        
        // For each structure array, set up unique constraints
        for (String structName : structures.keySet())
        {
            StructInstance[] structArray = structures.get(structName);
            
            // Find the variable definition
            Variable varDef = findVariableDefinition(vars, structName);
            if (varDef == null) continue;
            
            // Find the struct definition
            NStruct structDef = findStructDefinition(varDef.type);
            if (structDef == null) continue;
            
            // For each entry in the struct, check if it's unique
            for (Entry entry : structDef.entry)
            {
                if (entry.unique != null && entry.unique)
                {
                    // Collect all instances of this member across the array
                    VariableInstance[] uniqueGroup = new VariableInstance[structArray.length];
                    for (int i = 0; i < structArray.length; i++)
                    {
                        uniqueGroup[i] = structArray[i].members.get(entry.name);
                    }
                    
                    // Set each variable's uniqueWith to all others
                    for (int i = 0; i < uniqueGroup.length; i++)
                    {
                        // Create array of all others (excluding self)
                        VariableInstance[] others = new VariableInstance[uniqueGroup.length - 1];
                        int index = 0;
                        for (int j = 0; j < uniqueGroup.length; j++)
                        {
                            if (i != j)
                            {
                                others[index++] = uniqueGroup[j];
                            }
                        }
                        uniqueGroup[i].uniqueWith = others;
                    }
                }
            }
        }
    }
    
    /**
     * Find a variable definition by name.
     */
    private Variable findVariableDefinition(Variables vars, String name)
    {
        for (Variable var : vars.variable)
        {
            if (var.variableName.equals(name))
            {
                return var;
            }
        }
        return null;
    }
    
    /**
     * Print all variables and their current values.
     * Format: varName[index].member = value
     */
    private void printAllVariables()
    {
        // Print simple variables
        for (String varName : variables.keySet())
        {
            VariableInstance[] varArray = variables.get(varName);
            for (int i = 0; i < varArray.length; i++)
            {
                VariableInstance var = varArray[i];
                String[] options = definitions.get(var.definitionName);
                String value = options[var.currentValue];
                
                if (varArray.length == 1)
                {
                    System.out.println(varName + " = " + value);
                }
                else
                {
                    System.out.println(varName + "[" + i + "] = " + value);
                }
            }
        }
        
        // Print structures
        for (String structName : structures.keySet())
        {
            StructInstance[] structArray = structures.get(structName);
            for (int i = 0; i < structArray.length; i++)
            {
                StructInstance struct = structArray[i];
                for (String memberName : struct.members.keySet())
                {
                    VariableInstance var = struct.members.get(memberName);
                    String[] options = definitions.get(var.definitionName);
                    String value = options[var.currentValue];
                    
                    if (structArray.length == 1)
                    {
                        System.out.println(structName + "." + memberName + " = " + value);
                    }
                    else
                    {
                        System.out.println(structName + "[" + i + "]." + memberName + " = " + value);
                    }
                }
                if (i < structArray.length - 1)
                {
                    System.out.println();  // Blank line between struct instances
                }
            }
        }
    }
}

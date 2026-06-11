import AST.*;
import java.util.Optional;
import java.util.LinkedList;

public class NushaFall2025Parser {
    private TokenManager tokenManager;
    
    public NushaFall2025Parser()
    {

    }

    public Optional<Nusha> Nusha(LinkedList<Token> tokens) throws SyntaxErrorException {
        this.tokenManager = new TokenManager(tokens);
        
        Nusha nusha = new Nusha();
        
        // Skip any initial newlines
        while (tokenManager.MatchAndRemove(Token.TokenTypes.NEWLINE).isPresent())
        {
            // Continue consuming newlines
        }
        
        // Parse definitions first
        Optional<Definitions> definitions = Definitions();
        if (definitions.isPresent())
        {
            nusha.definitions = definitions.get();
        }
        else
        {
            nusha.definitions = new Definitions();
        }
        
        // Parse variable declarations
        Optional<Variables> variables = Variables();
        if (variables.isPresent())
        {
            nusha.variables = variables.get();
        }
        else
        {
            nusha.variables = new Variables();
        }
        
        // Parse rules
        Optional<Rules> rules = Rules();
        if (rules.isPresent()) {
            nusha.rules = rules.get();
        } else {
            nusha.rules = new Rules();
        }
        
        return Optional.of(nusha);
    }
    
    // Definitions = Definition*
    private Optional<Definitions> Definitions() throws SyntaxErrorException
    {
        Definitions definitions = new Definitions();
        
        while (!tokenManager.Done())
        {
            // Skip newlines
            while (tokenManager.MatchAndRemove(Token.TokenTypes.NEWLINE).isPresent())
            {

            }
            
            if (tokenManager.Done())
            {
                break;
            }
            
            // Check if this looks like a definition (IDENTIFIER = ...)
            Optional<Token> peek1 = tokenManager.Peek(0);
            Optional<Token> peek2 = tokenManager.Peek(1);
            
            if (peek1.isPresent() && peek1.get().Type == Token.TokenTypes.IDENTIFIER &&
                peek2.isPresent() && peek2.get().Type == Token.TokenTypes.EQUAL) {

                Optional<Definition> definition = Definition();
                if (definition.isPresent()) {
                    definitions.definition.add(definition.get());
                } else {
                    break;
                }
            }
            else
            {
                break; // Not a definition, move to next section
            }
        }

        return Optional.of(definitions);
    }
    
    // Definition = IDENTIFIER "=" (Choices | NStruct)
    private Optional<Definition> Definition() throws SyntaxErrorException {
        // Look for IDENTIFIER token
        Optional<Token> nameToken = tokenManager.MatchAndRemove(Token.TokenTypes.IDENTIFIER);
        if (!nameToken.isPresent()) {
            return Optional.empty();
        }
        
        // Expect equals
        if (!tokenManager.MatchAndRemove(Token.TokenTypes.EQUAL).isPresent()) {
            throw new SyntaxErrorException("Expected '=' after definition name", 
                tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
        }
        
        Definition definition = new Definition();
        definition.definitionName = nameToken.get().Value.orElse("");
        
        // Try parsing as Choices first (starts with {)
        Optional<Choices> choices = Choices();
        if (choices.isPresent()) {
            definition.choices = choices;
            definition.nstruct = Optional.empty();
        } else {
            // Try parsing as NStruct (starts with [)
            Optional<NStruct> nstruct = NStruct();
            if (nstruct.isPresent()) {
                definition.nstruct = nstruct;
                definition.choices = Optional.empty();
            } else {
                throw new SyntaxErrorException("Expected choices or struct definition", 
                    tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
            }
        }
        
        // Skip newlines after definition
        while (tokenManager.MatchAndRemove(Token.TokenTypes.NEWLINE).isPresent()) {
            // Continue consuming newlines
        }
        
        return Optional.of(definition);
    }
    
    // Choices = "{" IDENTIFIER ("," IDENTIFIER)* "}"
    private Optional<Choices> Choices() throws SyntaxErrorException {
        // Look for left curly brace
        if (!tokenManager.MatchAndRemove(Token.TokenTypes.LEFTCURLY).isPresent()) {
            return Optional.empty();
        }
        
        Choices choices = new Choices();
        
        // Get first identifier
        Optional<Token> firstChoice = tokenManager.MatchAndRemove(Token.TokenTypes.IDENTIFIER);
        if (!firstChoice.isPresent()) {
            throw new SyntaxErrorException("Expected identifier in choices", 
                tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
        }
        choices.choice.add(firstChoice.get().Value.orElse(""));
        
        // Parse additional choices separated by commas
        while (tokenManager.MatchAndRemove(Token.TokenTypes.COMMA).isPresent()) {
            Optional<Token> nextChoice = tokenManager.MatchAndRemove(Token.TokenTypes.IDENTIFIER);
            if (!nextChoice.isPresent()) {
                throw new SyntaxErrorException("Expected identifier after comma in choices", 
                    tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
            }
            choices.choice.add(nextChoice.get().Value.orElse(""));
        }
        
        // Expect right curly brace
        if (!tokenManager.MatchAndRemove(Token.TokenTypes.RIGHTCURLY).isPresent()) {
            throw new SyntaxErrorException("Expected '}' after choices", 
                tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
        }
        
        return Optional.of(choices);
    }
    
    // NStruct = "[" Entry ("," Entry)* "]"
    private Optional<NStruct> NStruct() throws SyntaxErrorException {
        // Look for left bracket
        if (!tokenManager.MatchAndRemove(Token.TokenTypes.LEFTBRACE).isPresent()) {
            return Optional.empty();
        }
        
        NStruct nstruct = new NStruct();
        
        // Get first entry
        Optional<Entry> firstEntry = Entry();
        if (!firstEntry.isPresent()) {
            throw new SyntaxErrorException("Expected entry in struct", 
                tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
        }
        nstruct.entry.add(firstEntry.get());
        
        // Parse additional entries separated by commas
        while (tokenManager.MatchAndRemove(Token.TokenTypes.COMMA).isPresent()) {
            Optional<Entry> nextEntry = Entry();
            if (!nextEntry.isPresent()) {
                throw new SyntaxErrorException("Expected entry after comma in struct", 
                    tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
            }
            nstruct.entry.add(nextEntry.get());
        }
        
        // Expect right bracket
        if (!tokenManager.MatchAndRemove(Token.TokenTypes.RIGHTBRACE).isPresent()) {
            throw new SyntaxErrorException("Expected ']' after struct", 
                tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
        }
        
        return Optional.of(nstruct);
    }
    
    // Entry = ("unique")? IDENTIFIER IDENTIFIER
    private Optional<Entry> Entry() throws SyntaxErrorException {
        Entry entry = new Entry();
        
        // Check for optional "unique" keyword
        Optional<Token> uniqueToken = tokenManager.MatchAndRemove(Token.TokenTypes.UNIQUE);
        entry.unique = uniqueToken.isPresent();
        
        // Get type identifier
        Optional<Token> typeToken = tokenManager.MatchAndRemove(Token.TokenTypes.IDENTIFIER);
        if (!typeToken.isPresent()) {
            return Optional.empty();
        }
        entry.type = typeToken.get().Value.orElse("");
        
        // Get name identifier
        Optional<Token> nameToken = tokenManager.MatchAndRemove(Token.TokenTypes.IDENTIFIER);
        if (!nameToken.isPresent()) {
            throw new SyntaxErrorException("Expected name identifier in entry", 
                tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
        }
        entry.name = nameToken.get().Value.orElse("");
        
        return Optional.of(entry);
    }
    
    // Variables = Variable*
    private Optional<Variables> Variables() throws SyntaxErrorException
    {
        Variables variables = new Variables();
        
        // Skip any newlines
        while (tokenManager.MatchAndRemove(Token.TokenTypes.NEWLINE).isPresent()) {
            // Continue consuming newlines
        }
        
        while (!tokenManager.Done())
        {
            // Check if we're looking at a variable declaration (starts with "var")
            Optional<Token> peek = tokenManager.Peek(0);
            if (!peek.isPresent() || peek.get().Type != Token.TokenTypes.VAR) {
                break; // Not a variable, move to rules section
            }
            
            Optional<Variable> variable = Variable();
            if (variable.isPresent())
            {
                variables.variable.add(variable.get());
            }
            else
            {
                break;
            }
        }
        
        return Optional.of(variables);
    }
    
    // Variable = "var" IDENTIFIER ":" IDENTIFIER "[" NUMBER "]"
    private Optional<Variable> Variable() throws SyntaxErrorException
    {
        // Look for "var" token
        if (!tokenManager.MatchAndRemove(Token.TokenTypes.VAR).isPresent())
        {
            return Optional.empty();
        }
        
        // Get variable name
        Optional<Token> varNameToken = tokenManager.MatchAndRemove(Token.TokenTypes.IDENTIFIER);
        if (!varNameToken.isPresent())
        {
            throw new SyntaxErrorException("Expected variable name after 'var'", 
                tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
        }
        
        // Expect colon
        if (!tokenManager.MatchAndRemove(Token.TokenTypes.COLON).isPresent())
        {
            throw new SyntaxErrorException("Expected ':' after variable name", 
                tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
        }
        
        // Get type name
        Optional<Token> typeToken = tokenManager.MatchAndRemove(Token.TokenTypes.IDENTIFIER);
        if (!typeToken.isPresent())
        {
            throw new SyntaxErrorException("Expected type name after ':'", 
                tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
        }
        
        // Expect left bracket
        if (!tokenManager.MatchAndRemove(Token.TokenTypes.LEFTBRACE).isPresent())
        {
            throw new SyntaxErrorException("Expected '[' after type name", 
                tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
        }
        
        // Get size
        Optional<Token> sizeToken = tokenManager.MatchAndRemove(Token.TokenTypes.NUMBER);
        if (!sizeToken.isPresent())
        {
            throw new SyntaxErrorException("Expected number in array size", 
                tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
        }
        
        // Expect right bracket
        if (!tokenManager.MatchAndRemove(Token.TokenTypes.RIGHTBRACE).isPresent())
        {
            throw new SyntaxErrorException("Expected ']' after array size", 
                tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
        }
        
        // Skip newlines after variable
        while (tokenManager.MatchAndRemove(Token.TokenTypes.NEWLINE).isPresent()) {
            // Continue consuming newlines
        }
        
        // Create Variable object
        Variable variable = new Variable();
        variable.variableName = varNameToken.get().Value.orElse("");
        variable.type = typeToken.get().Value.orElse("");
        variable.size = Optional.of(sizeToken.get().Value.orElse(""));
        
        return Optional.of(variable);
    }
    
    // Rules = Rule*
    private Optional<Rules> Rules() throws SyntaxErrorException {
        Rules rules = new Rules();
        
        while (!tokenManager.Done()) {
            // Skip newlines
            while (tokenManager.MatchAndRemove(Token.TokenTypes.NEWLINE).isPresent()) {
                // Continue consuming newlines
            }
            
            if (tokenManager.Done()) break;
            
            Optional<Rule> rule = Rule();
            if (rule.isPresent()) {
                rules.rule.add(rule.get());
            } else {
                break;
            }
        }
        
        return Optional.of(rules);
    }
    
    // Rule = Expression ("=>" Expression*)?  
    private Optional<Rule> Rule() throws SyntaxErrorException {
        Optional<Expression> expression = Expression();
        if (!expression.isPresent()) {
            return Optional.empty();
        }
        
        Rule rule = new Rule();
        rule.expression = expression.get();
        
        // Skip newlines after expression
        while (tokenManager.MatchAndRemove(Token.TokenTypes.NEWLINE).isPresent()) {
            // Continue consuming newlines
        }
        
        // Check for yields operator (=>)
        if (tokenManager.MatchAndRemove(Token.TokenTypes.YIELDS).isPresent()) {
            // Skip newlines after =>
            while (tokenManager.MatchAndRemove(Token.TokenTypes.NEWLINE).isPresent()) {
                // Continue consuming newlines
            }
            
            // Parse indented expressions
            if (tokenManager.MatchAndRemove(Token.TokenTypes.INDENT).isPresent()) {
                // Parse all expressions within this indented block
                while (true) {
                    Optional<Expression> thenExpression = Expression();
                    if (thenExpression.isPresent()) {
                        rule.thens.add(thenExpression.get());
                    } else {
                        break; // No more expressions
                    }
                    
                    // Skip newlines after then expression
                    while (tokenManager.MatchAndRemove(Token.TokenTypes.NEWLINE).isPresent()) {
                        // Continue consuming newlines
                    }
                    
                    // If we encounter DEDENT, we're done with this indented block
                    if (tokenManager.Peek(0).isPresent() && 
                        tokenManager.Peek(0).get().Type == Token.TokenTypes.DEDENT) {
                        break;
                    }
                }
                
                // Consume DEDENT if present
                tokenManager.MatchAndRemove(Token.TokenTypes.DEDENT);
            }
        }
        
        return Optional.of(rule);
    }
    
    // Expression = VariableReference Op VariableReference
    private Optional<Expression> Expression() throws SyntaxErrorException {
        Optional<VariableReference> left = VariableReference();
        if (!left.isPresent()) {
            return Optional.empty();
        }
        
        Optional<Op> op = Op();
        if (!op.isPresent()) {
            throw new SyntaxErrorException("Expected operator in expression", 
                tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
        }
        
        Optional<VariableReference> right = VariableReference();
        if (!right.isPresent()) {
            throw new SyntaxErrorException("Expected right operand in expression", 
                tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
        }
        
        Expression expression = new Expression();
        expression.left = left.get();
        expression.op = op.get();
        expression.right = right.get();
        
        return Optional.of(expression);
    }
    
    // VariableReference = IDENTIFIER VRModifier?
    private Optional<VariableReference> VariableReference() throws SyntaxErrorException {
        Optional<Token> nameToken = tokenManager.MatchAndRemove(Token.TokenTypes.IDENTIFIER);
        if (!nameToken.isPresent()) {
            return Optional.empty();
        }
        
        VariableReference varRef = new VariableReference();
        varRef.variableName = nameToken.get().Value.orElse("");
        
        Optional<VRModifier> modifier = VRModifier();
        varRef.vrmodifier = modifier;
        
        return Optional.of(varRef);
    }
    
    // VRModifier = ("[" NUMBER "]") | ("." IDENTIFIER) VRModifier?
    private Optional<VRModifier> VRModifier() throws SyntaxErrorException {
        VRModifier modifier = new VRModifier();
        
        // Check for array index [NUMBER]
        if (tokenManager.MatchAndRemove(Token.TokenTypes.LEFTBRACE).isPresent()) {
            Optional<Token> sizeToken = tokenManager.MatchAndRemove(Token.TokenTypes.NUMBER);
            if (!sizeToken.isPresent()) {
                throw new SyntaxErrorException("Expected number in array index", 
                    tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
            }
            
            if (!tokenManager.MatchAndRemove(Token.TokenTypes.RIGHTBRACE).isPresent()) {
                throw new SyntaxErrorException("Expected ']' after array index", 
                    tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
            }
            
            modifier.dot = false;
            modifier.size = sizeToken.get().Value.orElse("");
            modifier.part = Optional.empty();
            
            // Check for chained modifier
            Optional<VRModifier> chainedModifier = VRModifier();
            modifier.vrmodifier = chainedModifier;
            
            return Optional.of(modifier);
        }
        // Check for dot access .IDENTIFIER
        else if (tokenManager.MatchAndRemove(Token.TokenTypes.DOT).isPresent()) {
            Optional<Token> partToken = tokenManager.MatchAndRemove(Token.TokenTypes.IDENTIFIER);
            if (!partToken.isPresent()) {
                throw new SyntaxErrorException("Expected identifier after '.'", 
                    tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
            }
            
            modifier.dot = true;
            modifier.part = Optional.of(partToken.get().Value.orElse(""));
            modifier.size = null;
            
            // Check for chained modifier
            Optional<VRModifier> chainedModifier = VRModifier();
            modifier.vrmodifier = chainedModifier;
            
            return Optional.of(modifier);
        }
        
        return Optional.empty();
    }
    
    // Op = "=" | "!="
    private Optional<Op> Op() throws SyntaxErrorException {
        Op op = new Op();
        
        if (tokenManager.MatchAndRemove(Token.TokenTypes.EQUAL).isPresent()) {
            op.type = Op.OpTypes.Equal;
            return Optional.of(op);
        } else if (tokenManager.MatchAndRemove(Token.TokenTypes.NOTEQUAL).isPresent()) {
            op.type = Op.OpTypes.NotEqual;
            return Optional.of(op);
        }
        
        return Optional.empty();
    }
    
    private void RequireNewLine() throws SyntaxErrorException
    {
        // Allow one or more newlines
        boolean foundNewline = false;
        while (tokenManager.MatchAndRemove(Token.TokenTypes.NEWLINE).isPresent())
        {
            foundNewline = true;
        }
        
        // If we're at the beginning or end, we don't require a newline
        if (!foundNewline && !tokenManager.Done())
        {
            // Check if we're looking at a VAR token (start of next declaration)
            Optional<Token> peek = tokenManager.Peek(0);
            if (peek.isPresent() && peek.get().Type != Token.TokenTypes.VAR && peek.get().Type != Token.TokenTypes.YIELDS && peek.get().Type != Token.TokenTypes.INDENT && peek.get().Type != Token.TokenTypes.DEDENT)
            {
                throw new SyntaxErrorException("Expected newline", 
                    tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
            }
        }
    }
}

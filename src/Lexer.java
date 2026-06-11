import AST.*;
import java.util.LinkedList;
import java.util.Stack;

public class Lexer
{
    private TextManager textManager;
    private Stack<Integer> indentationStack;
    private boolean atStartOfLine;

    public Lexer(String input)
    {
        // Ensure input ends with newline
        if (!input.isEmpty() && input.charAt(input.length() - 1) != '\n')
        {
            input += "\n";
        }
        this.textManager = new TextManager(input);
        this.indentationStack = new Stack<>();
        this.indentationStack.push(0); // Base indentation level
        this.atStartOfLine = true;
    }

    public LinkedList<Token> Lex() throws SyntaxErrorException
    {
        LinkedList<Token> tokens = new LinkedList<>();

        while (!textManager.isAtEnd())
        {
            char currentChar = textManager.PeekCharacter();

            // Handle start of line - check for indentation
            if (atStartOfLine)
            {
                processIndentation(tokens);
                atStartOfLine = false;
                continue;
            }

            // Skip spaces and tabs (but not at start of line)
            if (currentChar == ' ' || currentChar == '\t')
            {
                textManager.GetCharacter();
                continue;
            }

            // Skip carriage returns
            if (currentChar == '\r')
            {
                textManager.GetCharacter();
                continue;
            }

            // Handle newlines
            if (currentChar == '\n')
            {
                int line = textManager.getLineNumber();
                int col = textManager.getCharacterPosition();
                textManager.GetCharacter();
                tokens.add(new Token(Token.TokenTypes.NEWLINE, line, col));
                atStartOfLine = true;
                continue;
            }

            // Handle numbers
            if (Character.isDigit(currentChar))
            {
                String number = readNumber();
                int line = textManager.getLineNumber();
                int col = textManager.getCharacterPosition() - number.length();
                tokens.add(new Token(Token.TokenTypes.NUMBER, line, col, number));
                continue;
            }

            // Handle identifiers and keywords
            if (Character.isLetter(currentChar))
            {
                String word = readWord();
                int line = textManager.getLineNumber();
                int col = textManager.getCharacterPosition() - word.length();
                Token token = createTokenFromWord(word, line, col);
                tokens.add(token);
                continue;
            }

            // Handle punctuation
            Token punctuationToken = handlePunctuation();
            if (punctuationToken != null)
            {
                tokens.add(punctuationToken);
                continue;
            }

            // If we reach here, we have an unknown character
            int line = textManager.getLineNumber();
            int col = textManager.getCharacterPosition();
            throw new SyntaxErrorException("Unknown character: " + currentChar, line, col);
        }

        // Generate final DEDENT tokens to close all remaining indentation levels
        while (indentationStack.size() > 1)
        {
            indentationStack.pop();
            int line = textManager.getLineNumber();
            int col = textManager.getCharacterPosition();
            tokens.add(new Token(Token.TokenTypes.DEDENT, line, col));
        }
        
        // Always add a final NEWLINE token
        int line = textManager.getLineNumber();
        int col = textManager.getCharacterPosition();
        tokens.add(new Token(Token.TokenTypes.NEWLINE, line, col));

        return tokens;
    }

    private void processIndentation(LinkedList<Token> tokens) throws SyntaxErrorException
    {
        int indentLevel = 0;
        
        // Count spaces and tabs at start of line
        while (!textManager.isAtEnd())
        {
            char ch = textManager.PeekCharacter();
            if (ch == ' ')
            {
                indentLevel++;
                textManager.GetCharacter();
            }
            else if (ch == '\t')
            {
                indentLevel += 4; // Tab counts as 4 spaces
                textManager.GetCharacter();
            }
            else
            {
                break;
            }
        }

        // If line is empty (only whitespace + newline), ignore indentation
        if (!textManager.isAtEnd() && textManager.PeekCharacter() == '\n')
        {
            return;
        }

        // Check that indentation is a multiple of 4
        if (indentLevel % 4 != 0)
        {
            int line = textManager.getLineNumber();
            int col = textManager.getCharacterPosition();
            throw new SyntaxErrorException("Indentation must be a multiple of 4 spaces", line, col);
        }

        int currentLevel = indentationStack.peek();
        
        if (indentLevel > currentLevel)
        {
            // Increased indentation - should be exactly one level (4 spaces)
            if (indentLevel != currentLevel + 4)
            {
                int line = textManager.getLineNumber();
                int col = textManager.getCharacterPosition();
                throw new SyntaxErrorException("Indentation can only increase by 4 spaces at a time", line, col);
            }
            indentationStack.push(indentLevel);
            int line = textManager.getLineNumber();
            int col = textManager.getCharacterPosition();
            tokens.add(new Token(Token.TokenTypes.INDENT, line, col));
        }
        else if (indentLevel < currentLevel)
        {
            // Decreased indentation - pop levels and generate DEDENT tokens
            while (!indentationStack.isEmpty() && indentationStack.peek() > indentLevel)
            {
                indentationStack.pop();
                int line = textManager.getLineNumber();
                int col = textManager.getCharacterPosition();
                tokens.add(new Token(Token.TokenTypes.DEDENT, line, col));
            }
            
            // Check that we dedented to a valid level
            if (indentationStack.isEmpty() || indentationStack.peek() != indentLevel)
            {
                int line = textManager.getLineNumber();
                int col = textManager.getCharacterPosition();
                throw new SyntaxErrorException("Invalid dedentation level", line, col);
            }
        }
        // If indentLevel == currentLevel, no indentation change
    }

    private String readNumber()
    {
        StringBuilder number = new StringBuilder();
        
        while (!textManager.isAtEnd())
        {
            char currentChar = textManager.PeekCharacter();
            if (Character.isDigit(currentChar))
            {
                number.append(textManager.GetCharacter());
            }
            else
            {
                break;
            }
        }
        
        return number.toString();
    }

    private String readWord()
    {
        StringBuilder word = new StringBuilder();

        while (!textManager.isAtEnd())
        {
            char currentChar = textManager.PeekCharacter();
            if (Character.isLetter(currentChar) || Character.isDigit(currentChar))
            {
                word.append(textManager.GetCharacter());
            }
            else
            {
                break;
            }
        }

        return word.toString();
    }

    private Token createTokenFromWord(String word, int line, int col)
    {
        switch (word)
        {
            case "unique":
                return new Token(Token.TokenTypes.UNIQUE, line, col);
            case "var":
                return new Token(Token.TokenTypes.VAR, line, col);
            default:
                return new Token(Token.TokenTypes.IDENTIFIER, line, col, word);
        }
    }

    private Token handlePunctuation() throws SyntaxErrorException
    {
        char currentChar = textManager.PeekCharacter();
        int line = textManager.getLineNumber();
        int col = textManager.getCharacterPosition();

        switch (currentChar)
        {
            case '=':
                textManager.GetCharacter();
                // Check for '=>'
                if (!textManager.isAtEnd() && textManager.PeekCharacter() == '>')
                {
                    textManager.GetCharacter();
                    return new Token(Token.TokenTypes.YIELDS, line, col);
                }
                return new Token(Token.TokenTypes.EQUAL, line, col);
                
            case '!':
                textManager.GetCharacter();
                // Check for '!='
                if (!textManager.isAtEnd() && textManager.PeekCharacter() == '=')
                {
                    textManager.GetCharacter();
                    return new Token(Token.TokenTypes.NOTEQUAL, line, col);
                }
                // '!' alone is not a valid token in this language
                throw new SyntaxErrorException("Unexpected character: !", line, col);
                
            case '{':
                textManager.GetCharacter();
                return new Token(Token.TokenTypes.LEFTCURLY, line, col);
                
            case '}':
                textManager.GetCharacter();
                return new Token(Token.TokenTypes.RIGHTCURLY, line, col);
                
            case '[':
                textManager.GetCharacter();
                return new Token(Token.TokenTypes.LEFTBRACE, line, col);
                
            case ']':
                textManager.GetCharacter();
                return new Token(Token.TokenTypes.RIGHTBRACE, line, col);
                
            case ',':
                textManager.GetCharacter();
                return new Token(Token.TokenTypes.COMMA, line, col);
                
            case ':':
                textManager.GetCharacter();
                return new Token(Token.TokenTypes.COLON, line, col);
                
            case '.':
                textManager.GetCharacter();
                return new Token(Token.TokenTypes.DOT, line, col);
                
            default:
                return null; // Not a punctuation character we handle
        }
    }
}

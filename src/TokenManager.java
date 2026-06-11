import AST.Token;
import java.util.LinkedList;
import java.util.Optional;

public class TokenManager
{
    private LinkedList<Token> tokens;
    private int currentIndex;

    public TokenManager(LinkedList<Token> tokens)
    {
        this.tokens = tokens;
        this.currentIndex = 0;
    }

    public int getCurrentLine()
    {
        if (tokens.isEmpty() || currentIndex >= tokens.size())
        {
            return 1; // Default to line 1 if no tokens
        }
        return tokens.get(currentIndex).LineNumber;
    }

    public int getCurrentColumnNumber()
    {
        if (tokens.isEmpty() || currentIndex >= tokens.size())
        {
            return 1; // Default to column 1 if no tokens
        }
        return tokens.get(currentIndex).ColumnNumber;
    }

    public int getLine()
    {
        return getCurrentLine();
    }

    public int getColumn()
    {
        return getCurrentColumnNumber();
    }

    public boolean Done()
    {
        return currentIndex >= tokens.size();
    }

    public Optional<Token> MatchAndRemove(Token.TokenTypes t)
    {
        if (Done() || tokens.get(currentIndex).Type != t)
        {
            return Optional.empty();
        }
        
        Token token = tokens.get(currentIndex);
        currentIndex++;
        return Optional.of(token);
    }

    public Optional<Token> Peek(int i)
    {
        int peekIndex = currentIndex + i;
        if (peekIndex < 0 || peekIndex >= tokens.size())
        {
            return Optional.empty();
        }
        return Optional.of(tokens.get(peekIndex));
    }
}

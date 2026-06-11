public class TextManager
{
    private String input;
    private int position;
    private int lineNumber;
    private int characterPosition;

    public TextManager(String input)
    {
        this.input = input;
        this.position = 0;
        this.lineNumber = 1;
        this.characterPosition = 1;
    }

    public boolean isAtEnd()
    {
        return position >= input.length();
    }

    public char PeekCharacter()
    {
        return PeekCharacter(0);
    }

    public char PeekCharacter(int dist)
    {
        int peekPosition = position + dist;
        if (peekPosition >= input.length()) {
            return '\0'; // Return null character if beyond end
        }
        return input.charAt(peekPosition);
    }

    public char GetCharacter()
    {
        if (isAtEnd())
        {
            return '\0';
        }
        char currentChar = input.charAt(position);
        position++;
        
        // Update line and character position
        if (currentChar == '\n')
        {
            lineNumber++;
            characterPosition = 1;
        }
        else
        {
            characterPosition++;
        }
        
        return currentChar;
    }
    
    public int getLineNumber()
    {
        return lineNumber;
    }
    
    public int getCharacterPosition()
    {
        return characterPosition;
    }
}


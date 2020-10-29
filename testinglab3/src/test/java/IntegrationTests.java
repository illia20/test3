import lex.LexicalAnalyzer;
import lex.workfile.IFileWorker;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileNotFoundException;

import static org.mockito.Mockito.*;

public class IntegrationTests {

    private static String okInFile = "src/main/resources/main.cpp";
    private static String badInFile = "0";

    @Mock
    private IFileWorker fwMock;
    @InjectMocks
    private static LexicalAnalyzer lexan;

    @BeforeTest
    public void initMocks(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void openFile() throws FileNotFoundException {
        when(fwMock.openFile(okInFile)).thenReturn(new File(okInFile));
        File f = lexan.setAndOpenInputFile(okInFile);
        Assert.assertNotNull(f);
        verify(fwMock).openFile(okInFile);
    }
    @Test(expectedExceptions = FileNotFoundException.class)
    public void openNFile() throws FileNotFoundException {
        when(fwMock.openFile(badInFile)).thenReturn(null);
        File f = lexan.setAndOpenInputFile(badInFile);
        System.out.println(f.length());
    }
    @Test
    public void openFileOnce() throws FileNotFoundException {
        when(fwMock.openFile(okInFile)).thenReturn(new File(okInFile));
        File f = lexan.setAndOpenInputFile(okInFile);
        Assert.assertNotNull(f);
        verify(fwMock, atLeastOnce()).openFile(okInFile);
    }
}

package de.pcc.privacycrashcam;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;

import de.pcc.privacycrashcam.data.Metadata;
import de.pcc.privacycrashcam.data.Settings;
import de.pcc.privacycrashcam.data.Video;
import de.pcc.privacycrashcam.data.memoryaccess.MemoryManager;
import de.pcc.privacycrashcam.testUtils.FileUtils;
import de.pcc.privacycrashcam.utils.datastructures.VideoRingBuffer;

import static org.mockito.Mockito.when;

/**
 * Mocks all necessary dependencies to be reused with all other test cases. If you need the mocks to
 * return more specific values you will need to add these with when(..).thenReturn(..). This class
 * just sets everything up so that the mocks return their default value.
 *
 * @author Giorgio Gross
 */
public class BaseTest {
    protected Context context;

    /**
     * Parent directory for all files needed for or created in this test
     */
    protected File testDirectory;
    private static final String DEFAULT_TEST_DIRECTORY_NAME = "testData";
    protected static final int CAPACITY = 3;
    protected ArrayBlockingQueue<File> mFiles = new ArrayBlockingQueue<>(CAPACITY);

    /**
     * Video data
     */
    protected static final String VIDEO_TAG = "123456789";
    protected static final long VIDEO_TAG_VAL = 123456789;
    protected static final String TEST_VIDEO = Video.PREFIX + VIDEO_TAG + "." + Video.SUFFIX;
    protected static final String VIDEO_TAG_TEMP = "1234567890";
    protected static final long VIDEO_TAG_TMEP_VAL = 1234567890;
    protected static final String TEST_VIDEO_TEMP = Video.PREFIX + VIDEO_TAG_TEMP + "." + Video.SUFFIX;
    @Mock
    protected VideoRingBuffer bufferMock;

    /**
     * Metadata
     */
    @Mock
    protected Metadata metadataMock;
    protected static final String TEST_METADATA_TEMP = Metadata.PREFIX_READABLE + VIDEO_TAG_TEMP + "." + Metadata.SUFFIX;
    protected static final String TEST_METADATA_R = Metadata.PREFIX_READABLE + VIDEO_TAG + "." + Metadata.SUFFIX;
    protected static final String TEST_METADATA = Metadata.PREFIX + VIDEO_TAG + "." + Metadata.SUFFIX;

    /**
     * Memory manager
     */
    @Mock
    protected MemoryManager memoryManagerMock;

    @Before
    public void init() throws Exception {
        context = InstrumentationRegistry.getTargetContext();
        testDirectory = context.getDir(DEFAULT_TEST_DIRECTORY_NAME, Context.MODE_PRIVATE);

        // init mocks is broken, so use the following line
        MockitoAnnotations.initMocks(this);

        // mock ring bufferMock
        for (int i = 0; i < CAPACITY; i++) {
            InputStream video = getClass().getClassLoader().getResourceAsStream("input_small.mp4");
            File videoDest = FileUtils.CreateFile(testDirectory, Video.PREFIX + i + "." + Video.SUFFIX);
            FileUtils.CopyInputStreamToFile(video, videoDest);
            mFiles.add(videoDest);
        }
        when(bufferMock.demandData()).thenReturn(mFiles);
        when(bufferMock.getCapacity()).thenReturn(CAPACITY);

        // mock memory manager
        when(memoryManagerMock.createReadableMetadataFile(VIDEO_TAG))
                .thenReturn(FileUtils.CreateFile(testDirectory, TEST_METADATA_R));
        when(memoryManagerMock.createEncryptedMetaFile(VIDEO_TAG)).
                thenReturn(FileUtils.CreateFile(testDirectory, TEST_METADATA));
        when(memoryManagerMock.createEncryptedVideoFile(VIDEO_TAG))
                .thenReturn(FileUtils.CreateFile(testDirectory, TEST_VIDEO));
        when(memoryManagerMock.getTempMetadataFile())
                .thenReturn(FileUtils.CreateFile(testDirectory, TEST_METADATA_TEMP));
        when(memoryManagerMock.getTempVideoFile())
                .thenReturn(FileUtils.CreateFile(testDirectory, TEST_VIDEO_TEMP));
        when(memoryManagerMock.getSettings()).thenReturn(new Settings());

        // mock metadataMock
        when(metadataMock.getDate()).thenReturn(VIDEO_TAG_VAL);
        when(metadataMock.getgForce()).thenReturn(new float[3]);
        when(metadataMock.getTriggerType()).thenReturn(Metadata.TRIGGER_TYPE_DEFAULT);
        when(metadataMock.getAsJSON()).thenReturn("{\n" +
                "  \"date\":123456789,\n" +
                "  \"triggerType\":\"NONE\",\n" +
                "  \"triggerForceX\":0,\n" +
                "  \"triggerForceY\":0,\n" +
                "  \"triggerForceZ\":0\n" +
                "}");

    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @After
    public void cleanUp() throws Exception {
        for (File file : testDirectory.listFiles()) {
            if (file != null) file.delete();
        }
    }



}
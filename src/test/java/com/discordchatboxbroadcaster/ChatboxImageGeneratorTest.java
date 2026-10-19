package com.discordchatboxbroadcaster;

import com.discordchatboxbroadcaster.config.BackgroundStyle;
import com.discordchatboxbroadcaster.config.ImageScale;
import com.discordchatboxbroadcaster.render.ChatSegment;
import com.discordchatboxbroadcaster.render.ChatSegment.Role;
import com.discordchatboxbroadcaster.render.ChatboxImageGenerator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChatboxImageGeneratorTest {

    @Mock
    private DiscordChatboxBroadcasterConfig config;

    private ChatboxImageGenerator generator;
    private AutoCloseable mockitoSession;

    @Before
    public void setUp() {
        mockitoSession = MockitoAnnotations.openMocks(this);
        when(config.showClanName()).thenReturn(true);
        when(config.showAccountIcon()).thenReturn(true);
        when(config.backgroundStyle()).thenReturn(BackgroundStyle.CLASSIC);
        when(config.highlightColor()).thenReturn(new Color(239, 16, 32));
        when(config.clanNameColor()).thenReturn(Color.BLUE);
        when(config.imageScale()).thenReturn(ImageScale.X1);

        generator = new ChatboxImageGenerator(config);
    }

    @Test
    public void testScalingDimensions() throws IOException {
        List<ChatSegment> segments = Arrays.asList(
                new ChatSegment("PlayerName: ", Role.TEXT),
                new ChatSegment("Hello Gielinor!", Role.HIGHLIGHT)
        );

        when(config.imageScale()).thenReturn(ImageScale.X1);
        byte[] bytes1x = generator.generateChatboxImage(segments);
        BufferedImage img1x = ImageIO.read(new ByteArrayInputStream(bytes1x));
        assertNotNull(img1x);
        int w1x = img1x.getWidth();
        int h1x = img1x.getHeight();
        assertEquals(30, h1x);

        when(config.imageScale()).thenReturn(ImageScale.X2);
        byte[] bytes2x = generator.generateChatboxImage(segments);
        BufferedImage img2x = ImageIO.read(new ByteArrayInputStream(bytes2x));
        assertNotNull(img2x);
        assertEquals(w1x * 2, img2x.getWidth());
        assertEquals(h1x * 2, img2x.getHeight());

        when(config.imageScale()).thenReturn(ImageScale.X3);
        byte[] bytes3x = generator.generateChatboxImage(segments);
        BufferedImage img3x = ImageIO.read(new ByteArrayInputStream(bytes3x));
        assertNotNull(img3x);
        assertEquals(w1x * 3, img3x.getWidth());
        assertEquals(h1x * 3, img3x.getHeight());
    }

    @Test
    public void testTransparentBackgroundCornerPixel() throws IOException {
        when(config.backgroundStyle()).thenReturn(BackgroundStyle.TRANSPARENT);
        List<ChatSegment> segments = Collections.singletonList(new ChatSegment("Test", Role.TEXT));

        byte[] bytes = generator.generateChatboxImage(segments);
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
        assertNotNull(img);

        int cornerRgb = img.getRGB(0, 0);
        int alpha = (cornerRgb >> 24) & 0xFF;
        assertEquals(0, alpha);
    }

    @Test
    public void testClassicBackgroundCornerPixel() throws IOException {
        when(config.backgroundStyle()).thenReturn(BackgroundStyle.CLASSIC);
        List<ChatSegment> segments = Collections.singletonList(new ChatSegment("Test", Role.TEXT));

        byte[] bytes = generator.generateChatboxImage(segments);
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
        assertNotNull(img);

        Color cornerColor = new Color(img.getRGB(0, 0), true);
        assertEquals(255, cornerColor.getAlpha());
        assertEquals(201, cornerColor.getRed());
        assertEquals(191, cornerColor.getGreen());
        assertEquals(169, cornerColor.getBlue());
    }

    @Test
    public void testDarkBackgroundCornerPixel() throws IOException {
        when(config.backgroundStyle()).thenReturn(BackgroundStyle.DARK);
        List<ChatSegment> segments = Collections.singletonList(new ChatSegment("Test", Role.TEXT));

        byte[] bytes = generator.generateChatboxImage(segments);
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
        assertNotNull(img);

        Color cornerColor = new Color(img.getRGB(0, 0), true);
        assertEquals(180, cornerColor.getAlpha());
        assertEquals(0, cornerColor.getRed());
        assertEquals(0, cornerColor.getGreen());
        assertEquals(0, cornerColor.getBlue());
    }

    @Test
    public void testShowClanNameToggle() throws IOException {
        List<ChatSegment> segments = Arrays.asList(
                new ChatSegment("[Iron Clan] ", Role.CLAN),
                new ChatSegment("Zezima ", Role.TEXT),
                new ChatSegment("has completed a quest!", Role.HIGHLIGHT)
        );

        when(config.showClanName()).thenReturn(true);
        byte[] withClanBytes = generator.generateChatboxImage(segments);
        BufferedImage withClanImg = ImageIO.read(new ByteArrayInputStream(withClanBytes));
        assertNotNull(withClanImg);

        when(config.showClanName()).thenReturn(false);
        byte[] withoutClanBytes = generator.generateChatboxImage(segments);
        BufferedImage withoutClanImg = ImageIO.read(new ByteArrayInputStream(withoutClanBytes));
        assertNotNull(withoutClanImg);

        assertTrue("Image with clan name must be wider than without clan name",
                withoutClanImg.getWidth() < withClanImg.getWidth());
    }

    @Test
    public void testShowAccountIconToggle() throws IOException {
        BufferedImage dummyIcon = new BufferedImage(12, 12, BufferedImage.TYPE_INT_ARGB);
        List<ChatSegment> segments = Arrays.asList(
                new ChatSegment(dummyIcon),
                new ChatSegment("PlayerName: ", Role.TEXT),
                new ChatSegment("Sample Message", Role.HIGHLIGHT)
        );

        when(config.showAccountIcon()).thenReturn(true);
        byte[] withIconBytes = generator.generateChatboxImage(segments);
        BufferedImage withIconImg = ImageIO.read(new ByteArrayInputStream(withIconBytes));
        assertNotNull(withIconImg);

        when(config.showAccountIcon()).thenReturn(false);
        byte[] withoutIconBytes = generator.generateChatboxImage(segments);
        BufferedImage withoutIconImg = ImageIO.read(new ByteArrayInputStream(withoutIconBytes));
        assertNotNull(withoutIconImg);

        assertTrue("Image with account icon must be wider than without account icon",
                withoutIconImg.getWidth() < withIconImg.getWidth());
    }

    @Test
    public void testNullConfigValuesDoNotThrow() throws IOException {
        DiscordChatboxBroadcasterConfig mockUnstubbed = mock(DiscordChatboxBroadcasterConfig.class);
        when(mockUnstubbed.backgroundStyle()).thenReturn(null);
        when(mockUnstubbed.highlightColor()).thenReturn(null);
        when(mockUnstubbed.clanNameColor()).thenReturn(null);
        when(mockUnstubbed.imageScale()).thenReturn(null);

        ChatboxImageGenerator generatorWithNullConfigValues = new ChatboxImageGenerator(mockUnstubbed);
        List<ChatSegment> segments = Arrays.asList(
                new ChatSegment("[Clan] ", Role.CLAN),
                new ChatSegment("Player: ", Role.TEXT),
                new ChatSegment("99 Strength", Role.HIGHLIGHT)
        );

        byte[] bytes = generatorWithNullConfigValues.generateChatboxImage(segments);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
        assertNotNull(img);

        ChatboxImageGenerator generatorNullConfig = new ChatboxImageGenerator(null);
        byte[] bytesNull = generatorNullConfig.generateChatboxImage(segments);
        assertNotNull(bytesNull);
        assertTrue(bytesNull.length > 0);
        BufferedImage imgNull = ImageIO.read(new ByteArrayInputStream(bytesNull));
        assertNotNull(imgNull);
    }

    @Test
    public void testEmptyAndNullSegmentsDoNotThrow() throws IOException {
        byte[] emptyBytes = generator.generateChatboxImage(Collections.emptyList());
        assertNotNull(emptyBytes);
        assertTrue(emptyBytes.length > 0);
        BufferedImage emptyImg = ImageIO.read(new ByteArrayInputStream(emptyBytes));
        assertNotNull(emptyImg);

        byte[] nullBytes = generator.generateChatboxImage(null);
        assertNotNull(nullBytes);
        assertTrue(nullBytes.length > 0);
        BufferedImage nullImg = ImageIO.read(new ByteArrayInputStream(nullBytes));
        assertNotNull(nullImg);
    }
}

package com.kcserver.kjfp.service.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FinanzausgleichMergeTest {
    private static PDFFinanzausgleichService service() {
        return mock(PDFFinanzausgleichService.class, CALLS_REAL_METHODS);
    }

    private static PDFFinanzausgleichService service(RecordingTempFiles temp) throws IOException {
        var service = service();
        doReturn(temp).when(service).createMergeTempFiles();
        return service;
    }

    @Test void mergeRetainsPageOrderBelegnummerGeometryAndRenderedContent() throws Exception {
        var temp = new RecordingTempFiles();
        try (var cover = document("Deckblatt", "Detail RZ_028");
             var attachments = document("Beleg Hil_001", "Beleg Hil_002")) {
            attachments.getPage(0).setRotation(90);
            attachments.getPage(1).setRotation(270);
            byte[] bytes = service(temp).mergeDocuments(cover, bytes(attachments));
            try (var result = Loader.loadPDF(bytes)) {
                assertEquals(4, result.getNumberOfPages());
                for (int i = 0; i < 4; i++) {
                    var source = i < 2 ? cover : attachments;
                    int index = i % 2;
                    var before = source.getPage(index);
                    var after = result.getPage(i);
                    assertEquals(before.getRotation(), after.getRotation());
                    assertEquals(before.getMediaBox().getWidth(), after.getMediaBox().getWidth());
                    assertEquals(before.getMediaBox().getHeight(), after.getMediaBox().getHeight());
                    assertEquals(before.getCropBox().getWidth(), after.getCropBox().getWidth());
                    assertEquals(before.getCropBox().getHeight(), after.getCropBox().getHeight());
                    assertEquals(pageText(source, index), pageText(result, i));
                    assertPixelsEqual(new PDFRenderer(source).renderImage(index), new PDFRenderer(result).renderImage(i));
                }
            }
            assertFalse(Files.exists(temp.directory));
            // The caller still owns and may use its cover document.
            assertEquals(2, cover.getNumberOfPages());
        } finally {
            temp.removeFaultsAndClean();
        }
    }

    @Test void footerStillNumbersCompleteMergedDocument() throws Exception {
        try (var cover = document("Deckblatt", "Detail RZ_028"); var attachments = document("Beleg Hil_001")) {
            try (var result = Loader.loadPDF(service().mergeDocuments(cover, bytes(attachments)))) {
                new PDFLayoutService().addFooter(result);
                assertEquals(3, result.getNumberOfPages());
                for (int i = 0; i < 3; i++) {
                    assertTrue(pageText(result, i).contains("Seite " + (i + 1) + " von 3"));
                }
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void nullOrEmptyAttachmentsStillProduceCoverAndCleanEverything(boolean empty) throws Exception {
        var temp = new RecordingTempFiles();
        try (var cover = document("Deckblatt")) {
            try (var result = Loader.loadPDF(service(temp).mergeDocuments(cover, empty ? new byte[0] : null))) {
                assertEquals(1, result.getNumberOfPages());
                assertEquals("Deckblatt", pageText(result, 0).strip());
            }
            assertEquals(2, temp.created.size());
            assertFalse(Files.exists(temp.directory));
        } finally {
            temp.removeFaultsAndClean();
        }
    }

    @Test void permissionsArePrivateBeforeAnyPdfIsWritten() throws Exception {
        try (var temp = new PDFFinanzausgleichService.MergeTempFiles()) {
            Path file = temp.createFile("permission-probe-");
            Path directory = file.getParent();
            if (Files.getFileStore(directory).supportsFileAttributeView("posix")) {
                assertEquals(PosixFilePermissions.fromString("rwx------"), Files.getPosixFilePermissions(directory));
                assertEquals(PosixFilePermissions.fromString("rw-------"), Files.getPosixFilePermissions(file));
            } else {
                // Runs without a skip on Windows/NTFS; compare effective ACL, not POSIX emulation.
                for (Path path : List.of(directory, file)) {
                    var view = Files.getFileAttributeView(path, AclFileAttributeView.class);
                    assertNotNull(view);
                    var acl = view.getAcl();
                    assertEquals(1, acl.size());
                    assertEquals(path.getFileSystem().getUserPrincipalLookupService()
                            .lookupPrincipalByName(System.getProperty("user.name")), acl.getFirst().principal());
                    assertEquals(java.nio.file.attribute.AclEntryType.ALLOW, acl.getFirst().type());
                    assertEquals(EnumSet.allOf(java.nio.file.attribute.AclEntryPermission.class), acl.getFirst().permissions());
                }
            }
        }
    }

    @Test void malformedAttachmentDoesNotLeaveSensitiveFiles() throws Exception {
        var temp = new RecordingTempFiles();
        try (var cover = document("Deckblatt")) {
            var service = service(temp);
            assertThrows(IOException.class, () -> service.mergeDocuments(cover, new byte[]{1, 2, 3}));
            assertEquals(3, temp.created.size());
            assertFalse(Files.exists(temp.directory));
            assertTrue(temp.created.stream().noneMatch(Files::exists));
        } finally {
            temp.removeFaultsAndClean();
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void failureAtEachFileCreationCleansEarlierFiles(int call) throws Exception {
        var temp = new RecordingTempFiles();
        temp.failCreationAt = call;
        IOException original = temp.creationFailure;
        try (var cover = document("Deckblatt"); var attachments = document("Beleg")) {
            var service = service(temp);
            byte[] attachmentBytes = bytes(attachments);
            assertSame(original, assertThrows(IOException.class,
                    () -> service.mergeDocuments(cover, attachmentBytes)));
            assertEquals(call - 1, temp.created.size());
            assertTrue(temp.created.stream().noneMatch(Files::exists));
            assertFalse(Files.exists(temp.directory));
        } finally {
            temp.removeFaultsAndClean();
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void oneDeleteFailureDoesNotPreventOtherDeletesAndIsReported(int fileIndex) throws Exception {
        var temp = new RecordingTempFiles();
        temp.failDeleteIndex = fileIndex;
        try (var cover = document("Deckblatt"); var attachments = document("Beleg")) {
            var service = service(temp);
            byte[] attachmentBytes = bytes(attachments);
            IOException failure = assertThrows(IOException.class,
                    () -> service.mergeDocuments(cover, attachmentBytes));
            assertEquals(4, temp.deleteAttempts.size());
            assertTrue(temp.deleteAttempts.containsAll(temp.created));
            assertEquals(temp.directory, temp.deleteAttempts.getLast());
            assertTrue(Arrays.stream(failure.getSuppressed()).anyMatch(e -> e == temp.deleteFailure));
            for (int i = 0; i < 3; i++) {
                assertEquals(i == fileIndex, Files.exists(temp.created.get(i)));
            }
            assertTrue(Files.exists(temp.directory)); // Failed file prevents directory removal; also reported.
            assertEquals(2, failure.getSuppressed().length);
        } finally {
            temp.removeFaultsAndClean();
        }
    }

    @Test void originalSaveFailureSurvivesCleanupFailures() throws Exception {
        var temp = new RecordingTempFiles();
        temp.failAllDeletes = true;
        IOException original = new IOException("simulated full disk during save");
        try (var cover = spy(document("Deckblatt"))) {
            doThrow(original).when(cover).save(any(File.class));
            var service = service(temp);
            assertSame(original, assertThrows(IOException.class, () -> service.mergeDocuments(cover, null)));
            assertEquals(3, temp.deleteAttempts.size());
            assertEquals(1, original.getSuppressed().length);
            assertEquals(3, original.getSuppressed()[0].getSuppressed().length);
        } finally {
            temp.removeFaultsAndClean();
        }
    }

    @Test void originalRuntimeFailureSurvivesAndSecurityExceptionDoesNotStopCleanup() throws Exception {
        var temp = new RecordingTempFiles();
        temp.failDeleteWithSecurityException = true;
        var original = new IllegalStateException("save failed");
        try (var cover = spy(document("Deckblatt"))) {
            doThrow(original).when(cover).save(any(File.class));
            assertSame(original, assertThrows(IllegalStateException.class,
                    () -> service(temp).mergeDocuments(cover, null)));
            assertEquals(3, temp.deleteAttempts.size());
            assertEquals(3, original.getSuppressed()[0].getSuppressed().length);
        } finally {
            temp.removeFaultsAndClean();
        }
    }

    @Test void directoryCreationFailureIsPropagatedBeforeWritingPdfData() throws Exception {
        var service = service();
        var failure = new AccessDeniedException("temporary directory");
        doThrow(failure).when(service).createMergeTempFiles();
        try (var cover = spy(document("Deckblatt"))) {
            assertSame(failure, assertThrows(IOException.class, () -> service.mergeDocuments(cover, null)));
            verify(cover, never()).save(any(File.class));
        }
    }

    @Test void parallelRequestsHaveDistinctDirectoriesAndNeverMixDocuments() throws Exception {
        var service = service();
        var directories = ConcurrentHashMap.<Path>newKeySet();
        doAnswer(invocation -> {
            var temp = new RecordingTempFiles();
            assertTrue(directories.add(temp.directory));
            return temp;
        }).when(service).createMergeTempFiles();
        try (var executor = Executors.newFixedThreadPool(4)) {
            List<Callable<Void>> requests = new ArrayList<>();
            for (int i = 0; i < 12; i++) {
                final int id = i;
                requests.add(() -> {
                    try (var cover = document("Deckblatt " + id); var attachment = document("Beleg " + id)) {
                        try (var result = Loader.loadPDF(service.mergeDocuments(cover, bytes(attachment)))) {
                            assertEquals(2, result.getNumberOfPages());
                            assertEquals("Deckblatt " + id, pageText(result, 0).strip());
                            assertEquals("Beleg " + id, pageText(result, 1).strip());
                        }
                    }
                    return null;
                });
            }
            for (var future : executor.invokeAll(requests, 30, TimeUnit.SECONDS)) {
                future.get();
            }
        }
        assertEquals(12, directories.size());
        assertTrue(directories.stream().noneMatch(Files::exists));
    }

    private static PDDocument document(String... texts) throws IOException {
        var document = new PDDocument();
        for (int i = 0; i < texts.length; i++) {
            var page = new PDPage(i % 2 == 0 ? PDRectangle.A4 : new PDRectangle(842, 595));
            document.addPage(page);
            try (var stream = new PDPageContentStream(document, page)) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 14);
                stream.newLineAtOffset(40, 120);
                stream.showText(texts[i]);
                stream.endText();
            }
        }
        return document;
    }

    private static byte[] bytes(PDDocument document) throws IOException {
        var out = new ByteArrayOutputStream();
        document.save(out);
        return out.toByteArray();
    }

    private static String pageText(PDDocument document, int index) throws IOException {
        var stripper = new PDFTextStripper();
        stripper.setStartPage(index + 1);
        stripper.setEndPage(index + 1);
        return stripper.getText(document);
    }

    private static void assertPixelsEqual(BufferedImage expected, BufferedImage actual) {
        assertEquals(expected.getWidth(), actual.getWidth());
        assertEquals(expected.getHeight(), actual.getHeight());
        assertArrayEquals(expected.getRGB(0, 0, expected.getWidth(), expected.getHeight(), null, 0, expected.getWidth()),
                actual.getRGB(0, 0, actual.getWidth(), actual.getHeight(), null, 0, actual.getWidth()));
    }

    static class RecordingTempFiles extends PDFFinanzausgleichService.MergeTempFiles {
        final Path directory;
        final List<Path> created = new ArrayList<>();
        final List<Path> deleteAttempts = new ArrayList<>();
        final IOException creationFailure = new IOException("simulated file creation failure");
        final IOException deleteFailure = new IOException("simulated delete failure");
        int creationCalls;
        int failCreationAt = -1;
        int failDeleteIndex = -1;
        boolean failAllDeletes;
        boolean failDeleteWithSecurityException;

        RecordingTempFiles() throws IOException {
            directory = (Path) ReflectionTestUtils.getField(this,
                    PDFFinanzausgleichService.MergeTempFiles.class, "directory");
        }

        @Override Path allocateFile(String prefix) throws IOException {
            if (++creationCalls == failCreationAt) throw creationFailure;
            Path file = super.allocateFile(prefix);
            created.add(file);
            return file;
        }

        @Override void delete(Path path) throws IOException {
            deleteAttempts.add(path);
            if (failDeleteWithSecurityException) throw new SecurityException("simulated ACL denial");
            if (failAllDeletes || failDeleteIndex >= 0 && path.equals(created.get(failDeleteIndex))) {
                throw deleteFailure;
            }
            super.delete(path);
        }

        void removeFaultsAndClean() throws IOException {
            failAllDeletes = false;
            failDeleteWithSecurityException = false;
            failDeleteIndex = -1;
            close();
        }
    }
}

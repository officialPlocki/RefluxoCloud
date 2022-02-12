package me.refluxo.cloud.util.files;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public abstract class FileConfiguration extends MemoryConfiguration {

    public static final boolean UTF8_OVERRIDE;

    public static final boolean UTF_BIG;

    public static final boolean SYSTEM_UTF;
    static {
        final byte[] testBytes = Base64Coder.decode("ICEiIyQlJicoKSorLC0uLzAxMjM0NTY3ODk6Ozw9Pj9AQUJDREVGR0hJSktMTU5PUFFSU1RVVldYWVpbXF1eX2BhYmNkZWZnaGlqa2xtbm9wcXJzdHV2d3h5ent8fX4NCg==");
        final String testString = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\r\n";
        final Charset defaultCharset = Charset.defaultCharset();
        final String resultString = new String(testBytes, defaultCharset);
        final boolean trueUTF = defaultCharset.name().contains("UTF");
        UTF8_OVERRIDE = !testString.equals(resultString) || defaultCharset.equals(StandardCharsets.US_ASCII);
        SYSTEM_UTF = trueUTF || UTF8_OVERRIDE;
        UTF_BIG = trueUTF && UTF8_OVERRIDE;
    }

    public FileConfiguration() {
        super();
    }

    @SuppressWarnings("UnstableApiUsage")
    public void save(File file) throws IOException {
        Objects.requireNonNull(file, "File cannot be null");

        Files.createParentDirs(file);

        String data = saveToString();

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), UTF8_OVERRIDE && !UTF_BIG ? Charsets.UTF_8 : Charset.defaultCharset())) {
            writer.write(data);
        }
    }
    @SuppressWarnings("unused")
    public void save(String file) throws IOException {
        Objects.requireNonNull(file, "File cannot be null");

        save(new File(file));
    }

    public abstract String saveToString();

    public void load(File file) throws Exception {
        Objects.requireNonNull(file, "File cannot be null");

        final FileInputStream stream = new FileInputStream(file);

        load(new InputStreamReader(stream, UTF8_OVERRIDE && !UTF_BIG ? Charsets.UTF_8 : Charset.defaultCharset()));
    }

    @Deprecated
    public void load(InputStream stream) throws Exception {
        Objects.requireNonNull(stream, "Stream cannot be null");

        load(new InputStreamReader(stream, UTF8_OVERRIDE ? Charsets.UTF_8 : Charset.defaultCharset()));
    }

    public void load(Reader reader) throws Exception {
        BufferedReader input = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);

        StringBuilder builder = new StringBuilder();

        try {
            String line;

            while ((line = input.readLine()) != null) {
                builder.append(line);
                builder.append('\n');
            }
        } finally {
            input.close();
        }

        loadFromString(builder.toString());
    }

    @SuppressWarnings("unused")
    public void load(String file) throws Exception {
        Objects.requireNonNull(file, "File cannot be null");

        load(new File(file));
    }

    public abstract void loadFromString(String contents) throws Exception;

    public abstract String buildHeader();

    public FileConfigurationOptions options() {
        return new FileConfigurationOptions(this);
    }
}
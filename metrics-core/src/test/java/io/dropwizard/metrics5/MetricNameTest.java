package io.dropwizard.metrics5;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MetricNameTest {
    @Test
    void testEmpty() {
        assertThat(MetricName.EMPTY.getTags()).isEmpty();
        assertThat(MetricName.EMPTY.getKey()).isEmpty();

        assertThat(MetricName.build()).isEqualTo(MetricName.EMPTY);
        assertThat(MetricName.EMPTY.resolve((String) null)).isEqualTo(MetricName.EMPTY);
    }

    @Test
    void testEmptyResolve() {
        final MetricName name = MetricName.build();
        assertThat(name.resolve("foo")).isEqualTo(MetricName.build("foo"));
    }

    @Test
    void testResolveToEmpty() {
        final MetricName name = MetricName.build("foo");
        assertThat(name.resolve((String) null)).isEqualTo(MetricName.build("foo"));
    }

    @Test
    void testResolve() {
        final MetricName name = MetricName.build("foo");
        assertThat(name.resolve("bar")).isEqualTo(MetricName.build("foo.bar"));
    }

    @Test
    void testResolveBothEmpty() {
        final MetricName name = MetricName.build();
        assertThat(name.resolve((String) null)).isEqualTo(MetricName.EMPTY);
    }

    @Test
    void testAddTagsVarious() {
        final Map<String, String> refTags = Map.of("foo", "bar");
        final MetricName test = MetricName.EMPTY.tagged("foo", "bar");
        final MetricName test2 = MetricName.EMPTY.tagged(refTags);

        assertThat(test).isEqualTo(new MetricName("", refTags));
        assertThat(test.getTags()).isEqualTo(refTags);

        assertThat(test2).isEqualTo(new MetricName("", refTags));
        assertThat(test2.getTags()).isEqualTo(refTags);
    }

    @Test
    void testTaggedMoreArguments() {
        final Map<String, String> refTags = Map.of(
                "foo", "bar",
                "baz", "biz");
        assertThat(MetricName.EMPTY.tagged("foo", "bar", "baz", "biz").getTags()).isEqualTo(refTags);
    }

    @Test
    void testTaggedNotPairs() {
        assertThrows(IllegalArgumentException.class, () -> MetricName.EMPTY.tagged("foo"));
    }

    @Test
    void testTaggedNotPairs2() {
        assertThrows(IllegalArgumentException.class, () -> MetricName.EMPTY.tagged("foo", "bar", "baz"));
    }

    @Test
    void testCompareTo() {
        final MetricName a = MetricName.EMPTY.tagged("foo", "bar");
        final MetricName b = MetricName.EMPTY.tagged("foo", "baz");

        assertThat(a.compareTo(b)).isLessThan(0);
        assertThat(b.compareTo(a)).isGreaterThan(0);
        assertThat(b.resolve("key").compareTo(b)).isGreaterThan(0);
        assertThat(b.compareTo(b.resolve("key"))).isLessThan(0);
    }

    @Test
    void testCompareTo2() {
        final MetricName a = MetricName.EMPTY.tagged("a", "x");
        final MetricName b = MetricName.EMPTY.tagged("b", "x");

        assertThat(MetricName.EMPTY.compareTo(a)).isLessThan(0);
        assertThat(MetricName.EMPTY.compareTo(b)).isLessThan(0);
        assertThat(a.compareTo(b)).isLessThan(0);
        assertThat(b.compareTo(a)).isGreaterThan(0);
    }

    @Test
    void testToStringWithSpecialCharacters() {
        // Test metric name with special characters that need escaping
        final MetricName specialKey = MetricName.build("metric,with=special chars");
        assertThat(specialKey.toString()).isEqualTo("metric\\,with\\=special\\ chars");

        // Test tags with special characters
        final MetricName specialTags = MetricName.build("metric")
                .tagged("key,with=comma", "value with spaces")
                .tagged("equals=key", "comma,value");
        assertThat(specialTags.toString()).isEqualTo("metric,equals\\=key=comma\\,value,key\\,with\\=comma=value\\ with\\ spaces");

        // Test backslashes
        final MetricName backslashes = MetricName.build("path\\to\\metric")
                .tagged("path", "C:\\Windows\\System32");
        assertThat(backslashes.toString()).isEqualTo("path\\\\to\\\\metric,path=C:\\\\Windows\\\\System32");
    }

    @Test
    void testParseWithSpecialCharacters() {
        // Test escaped special characters in key
        MetricName parsed = MetricName.parse("metric\\,with\\=special\\ chars");
        assertThat(parsed.getKey()).isEqualTo("metric,with=special chars");
        assertThat(parsed.getTags()).isEmpty();

        // Test escaped special characters in tags
        parsed = MetricName.parse("metric,key\\,with\\=comma=value\\ with\\ spaces,equals\\=key=comma\\,value");
        assertThat(parsed.getKey()).isEqualTo("metric");
        assertThat(parsed.getTags()).hasSize(2);
        assertThat(parsed.getTags().get("key,with=comma")).isEqualTo("value with spaces");
        assertThat(parsed.getTags().get("equals=key")).isEqualTo("comma,value");

        // Test backslashes
        parsed = MetricName.parse("path\\\\to\\\\metric,path=C:\\\\Windows\\\\System32");
        assertThat(parsed.getKey()).isEqualTo("path\\to\\metric");
        assertThat(parsed.getTags()).hasSize(1);
        assertThat(parsed.getTags().get("path")).isEqualTo("C:\\Windows\\System32");
    }

    @Test
    void testParseEmptyAndNull() {
        // Test null input
        MetricName parsed = MetricName.parse(null);
        assertThat(parsed).isEqualTo(MetricName.EMPTY);

        // Test empty string
        parsed = MetricName.parse("");
        assertThat(parsed).isEqualTo(MetricName.EMPTY);
    }

    @Test
    void testParseInvalidFormat() {
        // Test invalid tag format (no equals sign)
        assertThrows(IllegalArgumentException.class, () -> MetricName.parse("metric,invalidtag"));

        // Test invalid tag format (empty tag key)
        assertThrows(IllegalArgumentException.class, () -> MetricName.parse("metric,=value"));
    }

    @Test
    void testMultipleRoundTrips() {
        // Test multiple round-trips to ensure stability
        MetricName original = MetricName.build("complex.metric")
                .tagged("environment", "production")
                .tagged("version", "1.2.3")
                .tagged("datacenter", "us-east-1a");

        MetricName current = original;

        // Perform multiple round-trips
        for (int i = 0; i < 5; i++) {
            String stringRep = current.toString();
            current = MetricName.parse(stringRep);
            assertThat(current).isEqualTo(original);
        }
    }

    @Test
    void testUnicodeCharactersInKeysAndValues() {
        // Test Unicode characters in metric key
        MetricName unicodeKey = MetricName.build("métriques_データ_метрики");
        assertThat(unicodeKey.toString()).isEqualTo("métriques_データ_метрики");

        // Test Unicode characters in tag keys and values
        MetricName unicodeTags = MetricName.build("metrics")
                .tagged("環境", "プロダクション")
                .tagged("régión", "amérique")
                .tagged("статус", "работает");

        String expected = "metrics,régión=amérique,статус=работает,環境=プロダクション";
        assertThat(unicodeTags.toString()).isEqualTo(expected);

        // Round-trip test for Unicode
        MetricName parsed = MetricName.parse(unicodeTags.toString());
        assertThat(parsed).isEqualTo(unicodeTags);
        assertThat(parsed.getTags().get("環境")).isEqualTo("プロダクション");
        assertThat(parsed.getTags().get("régión")).isEqualTo("amérique");
        assertThat(parsed.getTags().get("статус")).isEqualTo("работает");
    }

    @Test
    void testEmojiCharactersInKeysAndValues() {
        // Test emoji characters in metric key
        MetricName emojiKey = MetricName.build("metrics🚀performance📊");
        assertThat(emojiKey.toString()).isEqualTo("metrics🚀performance📊");

        // Test emoji characters in tag keys and values
        MetricName emojiTags = MetricName.build("status")
                .tagged("mood😊", "happy😄")
                .tagged("alert🚨", "critical⚠️")
                .tagged("env🌍", "production🏭");

        String expected = "status,alert🚨=critical⚠️,env🌍=production🏭,mood😊=happy😄";
        assertThat(emojiTags.toString()).isEqualTo(expected);

        // Round-trip test for emojis
        MetricName parsed = MetricName.parse(emojiTags.toString());
        assertThat(parsed).isEqualTo(emojiTags);
        assertThat(parsed.getTags().get("mood😊")).isEqualTo("happy😄");
        assertThat(parsed.getTags().get("alert🚨")).isEqualTo("critical⚠️");
        assertThat(parsed.getTags().get("env🌍")).isEqualTo("production🏭");
    }

    @Test
    void testEqualsAndCommasInTagKeysAndValues() {
        // Test metric with equals and commas in tag keys
        MetricName specialTagKeys = MetricName.build("metrics")
                .tagged("key=with=equals", "simple_value")
                .tagged("key,with,commas", "another_value")
                .tagged("key=and,mixed", "mixed_value");

        String stringRep = specialTagKeys.toString();
        assertThat(stringRep).contains("key\\=with\\=equals=simple_value");
        assertThat(stringRep).contains("key\\,with\\,commas=another_value");
        assertThat(stringRep).contains("key\\=and\\,mixed=mixed_value");

        // Round-trip test
        MetricName parsed = MetricName.parse(stringRep);
        assertThat(parsed).isEqualTo(specialTagKeys);
        assertThat(parsed.getTags().get("key=with=equals")).isEqualTo("simple_value");
        assertThat(parsed.getTags().get("key,with,commas")).isEqualTo("another_value");
        assertThat(parsed.getTags().get("key=and,mixed")).isEqualTo("mixed_value");

        // Test metric with equals and commas in tag values
        MetricName specialTagValues = MetricName.build("metrics")
                .tagged("config", "key=value,another=setting")
                .tagged("query", "SELECT * FROM table WHERE id=1,status=active")
                .tagged("list", "item1,item2,item3=default");

        stringRep = specialTagValues.toString();
        assertThat(stringRep).contains("config=key\\=value\\,another\\=setting");
        assertThat(stringRep).contains("query=SELECT\\ *\\ FROM\\ table\\ WHERE\\ id\\=1\\,status\\=active");
        assertThat(stringRep).contains("list=item1\\,item2\\,item3\\=default");

        // Round-trip test
        parsed = MetricName.parse(stringRep);
        assertThat(parsed).isEqualTo(specialTagValues);
        assertThat(parsed.getTags().get("config")).isEqualTo("key=value,another=setting");
        assertThat(parsed.getTags().get("query")).isEqualTo("SELECT * FROM table WHERE id=1,status=active");
        assertThat(parsed.getTags().get("list")).isEqualTo("item1,item2,item3=default");
    }

    @Test
    void testComplexSpecialCharacterCombinations() {
        // Test complex combinations of special characters, Unicode, and emojis
        MetricName complex = MetricName.build("complex🔥metric,with=special")
                .tagged("user🧑‍💻", "admin=true,role=super")
                .tagged("config=app", "db=mysql,host=localhost")
                .tagged("日本語=キー", "値=テスト,環境=本番")
                .tagged("emoji🎯=target", "🏆winner,🥇first=place");

        // Verify round-trip works correctly
        String stringRep = complex.toString();
        MetricName parsed = MetricName.parse(stringRep);
        assertThat(parsed).isEqualTo(complex);

        // Verify specific values are preserved correctly
        assertThat(parsed.getKey()).isEqualTo("complex🔥metric,with=special");
        assertThat(parsed.getTags().get("user🧑‍💻")).isEqualTo("admin=true,role=super");
        assertThat(parsed.getTags().get("config=app")).isEqualTo("db=mysql,host=localhost");
        assertThat(parsed.getTags().get("日本語=キー")).isEqualTo("値=テスト,環境=本番");
        assertThat(parsed.getTags().get("emoji🎯=target")).isEqualTo("🏆winner,🥇first=place");
    }

    // Parameterized test data providers
    private static Stream<Arguments> nonPrintableCharacterData() {
        return Stream.of(
                Arguments.of('\n', "\\n", "newlines"),
                Arguments.of('\t', "\\t", "tabs"),
                Arguments.of('\r', "\\r", "carriagereturns")
        );
    }

    private static Stream<Arguments> parseErrorData() {
        return Stream.of(
                Arguments.of("metric\\", "incomplete escape sequence at end"),
                Arguments.of("metric,key=value\\", "incomplete escape sequence in tag value"),
                Arguments.of("metric\\uZZZZ", "invalid unicode escape sequence"),
                Arguments.of("metric\\u123", "incomplete unicode escape sequence"),
                Arguments.of("metric,=value", "empty tag key"),
                Arguments.of("metric,,key=value", "consecutive commas"),
                Arguments.of("metric,key=value,", "trailing comma")
        );
    }

    private static Stream<Arguments> roundTripTestData() {
        return Stream.of(
                Arguments.of(MetricName.build("http.requests"), "simple metric name"),
                Arguments.of(MetricName.build("http.requests").tagged("method", "GET"), "single tag"),
                Arguments.of(MetricName.build("database.queries")
                        .tagged("database", "users")
                        .tagged("action", "select")
                        .tagged("environment", "production"), "multiple tags"),
                Arguments.of(MetricName.EMPTY.tagged("env", "production").tagged("region", "eu-west"), "empty key with tags"),
                Arguments.of(MetricName.build("path\\to,metric=test")
                        .tagged("key,with=special", "value with spaces")
                        .tagged("backslash\\key", "backslash\\value"), "special characters"),
                Arguments.of(MetricName.EMPTY, "empty metric name")
        );
    }

    private static Stream<Arguments> constructorTestData() {
        return Stream.of(
                Arguments.of("", Collections.emptyMap(), "empty key, empty tags"),
                Arguments.of("metric", Collections.emptyMap(), "simple key, empty tags"),
                Arguments.of("metric", null, "simple key, null tags"),
                Arguments.of("complex.metric.name", Map.of("env", "prod", "region", "us"), "complex key, multiple tags")
        );
    }

    private static Stream<Arguments> multipleRoundTripData() {
        return Stream.of(
                Arguments.of(MetricName.build("test").tagged("key", "value"), 5),
                Arguments.of(MetricName.build("complex🔥metric,with=special")
                        .tagged("user🧑‍💻", "admin=true,role=super"), 3),
                Arguments.of(MetricName.build("metric\n\t\rwith\nnon\tprintable\rchars"), 10)
        );
    }

    @ParameterizedTest(name = "Non-printable character {2} in metric keys")
    @MethodSource("nonPrintableCharacterData")
    void testNonPrintableCharactersInKeys(char character, String escaped, String description) {
        // Test character in metric key
        MetricName metricWithChar = MetricName.build("metric" + character + "with" + character + description);
        String stringRep = metricWithChar.toString();
        assertThat(stringRep).isEqualTo("metric" + escaped + "with" + escaped + description);

        MetricName parsed = MetricName.parse(stringRep);
        assertThat(parsed).isEqualTo(metricWithChar);
        assertThat(parsed.getKey()).isEqualTo("metric" + character + "with" + character + description);
    }

    @ParameterizedTest(name = "Non-printable character {2} in tag keys")
    @MethodSource("nonPrintableCharacterData")
    void testNonPrintableCharactersInTagKeys(char character, String escaped, String description) {
        // Test character in tag key
        MetricName metricWithTagKey = MetricName.build("metric").tagged("key" + character + "with" + character + description, "value");
        String stringRep = metricWithTagKey.toString();
        assertThat(stringRep).isEqualTo("metric,key" + escaped + "with" + escaped + description + "=value");

        MetricName parsed = MetricName.parse(stringRep);
        assertThat(parsed).isEqualTo(metricWithTagKey);
        assertThat(parsed.getTags().get("key" + character + "with" + character + description)).isEqualTo("value");
    }

    @ParameterizedTest(name = "Non-printable character {2} in tag values")
    @MethodSource("nonPrintableCharacterData")
    void testNonPrintableCharactersInTagValues(char character, String escaped, String description) {
        // Test character in tag value
        MetricName metricWithTagValue = MetricName.build("metric").tagged("key", "value" + character + "with" + character + description);
        String stringRep = metricWithTagValue.toString();
        assertThat(stringRep).isEqualTo("metric,key=value" + escaped + "with" + escaped + description);

        MetricName parsed = MetricName.parse(stringRep);
        assertThat(parsed).isEqualTo(metricWithTagValue);
        assertThat(parsed.getTags().get("key")).isEqualTo("value" + character + "with" + character + description);
    }

    @ParameterizedTest(name = "Parse error: {1}")
    @MethodSource("parseErrorData")
    void testParseErrors(String input, String description) {
        assertThrows(IllegalArgumentException.class, () -> MetricName.parse(input));
    }

    @ParameterizedTest(name = "Round-trip test: {1}")
    @MethodSource("roundTripTestData")
    void testRoundTripConversion(MetricName original, String description) {
        String stringRepresentation = original.toString();
        MetricName parsed = MetricName.parse(stringRepresentation);
        assertThat(parsed).isEqualTo(original);
    }

    @ParameterizedTest(name = "Constructor test: {2}")
    @MethodSource("constructorTestData")
    void testConstructorVariations(String key, Map<String, String> tags, String description) {
        MetricName metricName = new MetricName(key, tags);
        assertThat(metricName.getKey()).isEqualTo(key);
        if (tags == null) {
            assertThat(metricName.getTags()).isEmpty();
        } else {
            assertThat(metricName.getTags()).isEqualTo(tags);
        }
    }

    @ParameterizedTest(name = "Multiple round-trips with {1} iterations")
    @MethodSource("multipleRoundTripData")
    void testMultipleRoundTrips(MetricName original, int iterations) {
        MetricName current = original;
        for (int i = 0; i < iterations; i++) {
            String stringRep = current.toString();
            current = MetricName.parse(stringRep);
            assertThat(current).isEqualTo(original);
        }
    }

    @Test
    void testEdgeCasesWithSpecialCharacters() {
        // Test edge case: tag key that is just special characters
        MetricName edgeCase1 = MetricName.build("metric").tagged("=,=", "value");
        String stringRep = edgeCase1.toString();
        MetricName parsed = MetricName.parse(stringRep);
        assertThat(parsed).isEqualTo(edgeCase1);
        assertThat(parsed.getTags().get("=,=")).isEqualTo("value");

        // Test edge case: tag value that is just special characters
        MetricName edgeCase2 = MetricName.build("metric").tagged("key", ",=,=");
        stringRep = edgeCase2.toString();
        parsed = MetricName.parse(stringRep);
        assertThat(parsed).isEqualTo(edgeCase2);
        assertThat(parsed.getTags().get("key")).isEqualTo(",=,=");

        // Test edge case: empty tag value
        MetricName edgeCase3 = MetricName.build("metric").tagged("empty", "");
        stringRep = edgeCase3.toString();
        parsed = MetricName.parse(stringRep);
        assertThat(parsed).isEqualTo(edgeCase3);
        assertThat(parsed.getTags().get("empty")).isEmpty();

        // Test edge case: tag key and value both containing all special characters
        MetricName edgeCase4 = MetricName.build("metric").tagged("\\,= ", "\\,= ");
        stringRep = edgeCase4.toString();
        parsed = MetricName.parse(stringRep);
        assertThat(parsed).isEqualTo(edgeCase4);
        assertThat(parsed.getTags().get("\\,= ")).isEqualTo("\\,= ");
    }

    @Test
    void testNonPrintableCharactersCombinedWithSpecialCharacters() {
        // Test combination of non-printable and special characters in metric key
        MetricName complexKey = MetricName.build("metric\n,with=special\t characters\r");
        String stringRep = complexKey.toString();
        assertThat(stringRep).isEqualTo("metric\\n\\,with\\=special\\t\\ characters\\r");

        MetricName parsed = MetricName.parse(stringRep);
        assertThat(parsed).isEqualTo(complexKey);
        assertThat(parsed.getKey()).isEqualTo("metric\n,with=special\t characters\r");

        // Test combination in tag keys and values
        MetricName complexTags = MetricName.build("metric")
                .tagged("key\n,with=special\t", "value\r with\n=mixed\t,chars")
                .tagged("another\tkey", "another\nvalue");

        stringRep = complexTags.toString();
        assertThat(stringRep).contains("another\\tkey=another\\nvalue");
        assertThat(stringRep).contains("key\\n\\,with\\=special\\t=value\\r\\ with\\n\\=mixed\\t\\,chars");

        parsed = MetricName.parse(stringRep);
        assertThat(parsed).isEqualTo(complexTags);
        assertThat(parsed.getTags().get("key\n,with=special\t")).isEqualTo("value\r with\n=mixed\t,chars");
        assertThat(parsed.getTags().get("another\tkey")).isEqualTo("another\nvalue");
    }

    @Test
    void testNonPrintableCharactersRoundTrip() {
        // Test extensive round-trip with various non-printable characters
        MetricName complex = MetricName.build("metric\n\t\rwith\nnon\tprintable\rchars")
                .tagged("key\nwith\nnewlines", "value\nwith\nnewlines")
                .tagged("key\twith\ttabs", "value\twith\ttabs")
                .tagged("key\rwith\rcarriage", "value\rwith\rcarriage")
                .tagged("mixed\n\t\rkey", "mixed\r\n\tvalue");

        // Perform multiple round-trips to ensure stability
        MetricName current = complex;
        for (int i = 0; i < 3; i++) {
            String stringRep = current.toString();
            current = MetricName.parse(stringRep);
            assertThat(current).isEqualTo(complex);
        }

        // Verify specific values are preserved correctly
        assertThat(current.getKey()).isEqualTo("metric\n\t\rwith\nnon\tprintable\rchars");
        assertThat(current.getTags().get("key\nwith\nnewlines")).isEqualTo("value\nwith\nnewlines");
        assertThat(current.getTags().get("key\twith\ttabs")).isEqualTo("value\twith\ttabs");
        assertThat(current.getTags().get("key\rwith\rcarriage")).isEqualTo("value\rwith\rcarriage");
        assertThat(current.getTags().get("mixed\n\t\rkey")).isEqualTo("mixed\r\n\tvalue");
    }

    @Test
    void testNonPrintableCharactersEdgeCases() {
        // Test edge case: only non-printable characters in key
        MetricName onlyNonPrintable = MetricName.build("\n\t\r");
        String stringRep = onlyNonPrintable.toString();
        assertThat(stringRep).isEqualTo("\\n\\t\\r");

        MetricName parsed = MetricName.parse(stringRep);
        assertThat(parsed).isEqualTo(onlyNonPrintable);
        assertThat(parsed.getKey()).isEqualTo("\n\t\r");

        // Test edge case: only non-printable characters in tag key and value
        MetricName onlyNonPrintableTags = MetricName.build("metric").tagged("\n\t\r", "\r\t\n");
        stringRep = onlyNonPrintableTags.toString();
        assertThat(stringRep).isEqualTo("metric,\\n\\t\\r=\\r\\t\\n");

        parsed = MetricName.parse(stringRep);
        assertThat(parsed).isEqualTo(onlyNonPrintableTags);
        assertThat(parsed.getTags().get("\n\t\r")).isEqualTo("\r\t\n");

        // Test edge case: consecutive escaping situations
        MetricName consecutiveEscapes = MetricName.build("metric")
                .tagged("key\\n", "value\\t")  // backslash followed by letter that looks like escape
                .tagged("another\nkey", "another\tvalue");  // actual non-printable characters

        stringRep = consecutiveEscapes.toString();
        parsed = MetricName.parse(stringRep);
        assertThat(parsed).isEqualTo(consecutiveEscapes);
        assertThat(parsed.getTags().get("key\\n")).isEqualTo("value\\t");
        assertThat(parsed.getTags().get("another\nkey")).isEqualTo("another\tvalue");
    }

    @Test
    void testNonPrintableCharactersWithUnicodeAndEmojis() {
        // Test combination of non-printable characters with Unicode and emojis
        MetricName unicodeWithNonPrintable = MetricName.build("métriques\nデータ\t🚀")
                .tagged("環境\n😊", "プロダクション\t🏭")
                .tagged("status\r🚨", "active\n⚠️");

        String stringRep = unicodeWithNonPrintable.toString();
        MetricName parsed = MetricName.parse(stringRep);

        assertThat(parsed).isEqualTo(unicodeWithNonPrintable);
        assertThat(parsed.getKey()).isEqualTo("métriques\nデータ\t🚀");
        assertThat(parsed.getTags().get("環境\n😊")).isEqualTo("プロダクション\t🏭");
        assertThat(parsed.getTags().get("status\r🚨")).isEqualTo("active\n⚠️");

        // Verify the string representation properly escapes non-printable chars
        assertThat(stringRep).contains("métriques\\nデータ\\t🚀");
        assertThat(stringRep).contains("環境\\n😊=プロダクション\\t🏭");
        assertThat(stringRep).contains("status\\r🚨=active\\n⚠️");
    }

    @Test
    void testConstructorNullKey() {
        assertThrows(NullPointerException.class, () -> new MetricName(null, Map.of()));
    }

    @Test
    void testConstructorEmptyKey() {
        // Empty key is allowed by the constructor
        MetricName metricName = new MetricName("", Map.of());
        assertThat(metricName.getKey()).isEmpty();
        assertThat(metricName.getTags()).isEmpty();
    }

    @Test
    void testConstructorBlankKey() {
        // Blank key is allowed by the constructor
        MetricName metricName = new MetricName("   ", Map.of());
        assertThat(metricName.getKey()).isEqualTo("   ");
        assertThat(metricName.getTags()).isEmpty();
    }

    @Test
    void testConstructorNullTags() {
        // Null tags are allowed by the constructor (converted to EMPTY_TAGS)
        MetricName metricName = new MetricName("key", null);
        assertThat(metricName.getKey()).isEqualTo("key");
        assertThat(metricName.getTags()).isEmpty();
    }

    @Test
    void testControlCharactersEscaping() {
        // Test ASCII control characters (0-31 and 127)
        MetricName controlChars = MetricName.build("metric\u0001\u0002\u007F");
        String stringRep = controlChars.toString();
        assertThat(stringRep).contains("\\u0001");
        assertThat(stringRep).contains("\\u0002");
        assertThat(stringRep).contains("\\u007f");

        MetricName parsed = MetricName.parse(stringRep);
        assertThat(parsed).isEqualTo(controlChars);
    }

    @Test
    void testControlCharactersInTags() {
        MetricName controlTags = MetricName.build("metric")
                .tagged("key\u0000", "value\u001F")
                .tagged("del\u007F", "test");

        String stringRep = controlTags.toString();
        MetricName parsed = MetricName.parse(stringRep);
        assertThat(parsed).isEqualTo(controlTags);
    }

    @Test
    void testLargeNumberOfTags() {
        Map<String, String> largeTags = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            largeTags.put("key" + i, "value" + i);
        }

        MetricName largeName = new MetricName("metric", largeTags);
        String stringRep = largeName.toString();
        MetricName parsed = MetricName.parse(stringRep);
        assertThat(parsed).isEqualTo(largeName);
    }

    @Test
    void testVeryLongKeysAndValues() {
        String longKey = "a".repeat(10000);
        String longValue = "b".repeat(10000);

        MetricName longName = MetricName.build(longKey).tagged("longTag", longValue);
        String stringRep = longName.toString();
        MetricName parsed = MetricName.parse(stringRep);
        assertThat(parsed).isEqualTo(longName);
    }

    @Test
    @SuppressWarnings("SelfAssertion")
    void testEqualsAndHashCode() {
        MetricName name1 = MetricName.build("test").tagged("env", "prod");
        MetricName name2 = MetricName.build("test").tagged("env", "prod");
        MetricName name3 = MetricName.build("test").tagged("env", "dev");

        assertThat(name1).isEqualTo(name2);
        assertThat(name1).isNotEqualTo(name3);
        assertThat(name1.hashCode()).isEqualTo(name2.hashCode());

        // Test equals with null and different types
        assertThat(name1).isNotEqualTo(null);
        assertThat(name1).isNotEqualTo("string");

        // Test self-equality
        assertThat(name1).isEqualTo(name1);
    }

    @Test
    void testEqualsWithDifferentTagOrder() {
        MetricName name1 = MetricName.build("test").tagged("a", "1").tagged("b", "2");
        MetricName name2 = MetricName.build("test").tagged("b", "2").tagged("a", "1");

        assertThat(name1).isEqualTo(name2);
        assertThat(name1.hashCode()).isEqualTo(name2.hashCode());
    }

    @Test
    void testTagsImmutability() {
        Map<String, String> originalTags = new HashMap<>();
        originalTags.put("env", "prod");

        MetricName name = new MetricName("test", originalTags);

        // Modify original map
        originalTags.put("new", "value");

        // MetricName should not be affected
        assertThat(name.getTags()).hasSize(1);
        assertThat(name.getTags()).containsKey("env");
        assertThat(name.getTags()).doesNotContainKey("new");

        // Returned map should be immutable
        assertThrows(UnsupportedOperationException.class, () -> name.getTags().put("another", "tag"));
    }

    @Test
    void testAppendMethod() {
        MetricName base = MetricName.build("base").tagged("env", "prod");
        MetricName append = MetricName.build("suffix").tagged("version", "1.0");

        MetricName result = base.append(append);

        assertThat(result.getKey()).isEqualTo("base.suffix");
        assertThat(result.getTags()).hasSize(2);
        assertThat(result.getTags().get("env")).isEqualTo("prod");
        assertThat(result.getTags().get("version")).isEqualTo("1.0");
    }

    @Test
    void testAppendWithConflictingTags() {
        MetricName base = MetricName.build("base").tagged("env", "prod");
        MetricName append = MetricName.build("suffix").tagged("env", "dev");

        MetricName result = base.append(append);

        // Append tags should override base tags
        assertThat(result.getTags().get("env")).isEqualTo("dev");
    }

    @Test
    void testResolveWithMultipleParts() {
        MetricName base = MetricName.build("base");
        MetricName result = base.resolve("part1", "part2", "part3");

        assertThat(result.getKey()).isEqualTo("base.part1.part2.part3");
    }

    @Test
    void testResolveWithNullAndEmptyParts() {
        MetricName base = MetricName.build("base");
        MetricName result = base.resolve(null, "", "valid", null, "");

        assertThat(result.getKey()).isEqualTo("base.valid");
    }

    @Test
    void testMaxIntegerTagValues() {
        MetricName name = MetricName.build("metric")
                .tagged("max", String.valueOf(Integer.MAX_VALUE))
                .tagged("min", String.valueOf(Integer.MIN_VALUE))
                .tagged("zero", "0");

        String stringRep = name.toString();
        MetricName parsed = MetricName.parse(stringRep);
        assertThat(parsed).isEqualTo(name);
    }

    @Test
    void testEmptyStringTagValues() {
        MetricName name = MetricName.build("metric")
                .tagged("empty", "")
                .tagged("space", " ")
                .tagged("tab", "\t");

        String stringRep = name.toString();
        MetricName parsed = MetricName.parse(stringRep);
        assertThat(parsed).isEqualTo(name);
        assertThat(parsed.getTags().get("empty")).isEmpty();
        assertThat(parsed.getTags().get("space")).isEqualTo(" ");
        assertThat(parsed.getTags().get("tab")).isEqualTo("\t");
    }

    @Test
    void testTaggedWithNullArguments() {
        MetricName nameWithNullArray = MetricName.build("test").tagged((String[]) null);
        assertThat(nameWithNullArray.getTags()).isEmpty();

        MetricName nameWithNullMap = MetricName.build("test").tagged((Map<String, String>) null);
        assertThat(nameWithNullMap.getTags()).isEmpty();
    }

    @Test
    void testTaggedWithEmptyMap() {
        MetricName original = MetricName.build("test").tagged("env", "prod");
        MetricName result = original.tagged(Map.of());

        assertThat(result).isEqualTo(original);
    }

    @Test
    void testBuildWithNullParts() {
        MetricName result = MetricName.build((String[]) null);
        assertThat(result).isEqualTo(MetricName.EMPTY);
    }

    @Test
    void testBuildWithEmptyArray() {
        MetricName result = MetricName.build(new String[0]);
        assertThat(result).isEqualTo(MetricName.EMPTY);
    }

    @Test
    void testComparabilityConsistency() {
        MetricName a = MetricName.build("a").tagged("x", "1");
        MetricName b = MetricName.build("b").tagged("x", "1");
        MetricName c = MetricName.build("c").tagged("x", "1");

        // Test transitivity: if a < b and b < c, then a < c
        assertThat(a.compareTo(b)).isLessThan(0);
        assertThat(b.compareTo(c)).isLessThan(0);
        assertThat(a.compareTo(c)).isLessThan(0);

        // Test consistency with equals
        MetricName a2 = MetricName.build("a").tagged("x", "1");
        assertThat(a.compareTo(a2)).isEqualTo(0);
        assertThat(a).isEqualTo(a2);
    }

    @Test
    void testCompareToWithDifferentNumberOfTags() {
        MetricName noTags = MetricName.build("metric");
        MetricName oneTags = MetricName.build("metric").tagged("a", "1");
        MetricName twoTags = MetricName.build("metric").tagged("a", "1").tagged("b", "2");

        assertThat(noTags.compareTo(oneTags)).isLessThan(0);
        assertThat(oneTags.compareTo(twoTags)).isLessThan(0);
        assertThat(noTags.compareTo(twoTags)).isLessThan(0);
    }

    @Test
    void testPerformanceWithLargeTags() {
        // Test performance doesn't degrade significantly with many tags
        Map<String, String> manyTags = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            manyTags.put("key" + String.format("%03d", i), "value" + i);
        }

        long startTime = System.currentTimeMillis();
        MetricName name = new MetricName("performance.test", manyTags);
        String stringRep = name.toString();
        MetricName parsed = MetricName.parse(stringRep);
        long endTime = System.currentTimeMillis();

        assertThat(parsed).isEqualTo(name);
        // Should complete within reasonable time (less than 1 second)
        assertThat(endTime - startTime).isLessThan(1000);
    }

    @Test
    void testThreadSafety() throws InterruptedException {
        MetricName name = MetricName.build("test").tagged("env", "prod");

        int threadCount = 10;
        int operationsPerThread = 100;
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        // Test concurrent toString() calls
                        String str = name.toString();
                        MetricName parsed = MetricName.parse(str);
                        if (!parsed.equals(name)) {
                            throw new AssertionError("Parsed name doesn't match original");
                        }

                        // Test concurrent tagged() calls
                        MetricName tagged = name.tagged("thread", String.valueOf(Thread.currentThread().getId()));
                        if (!tagged.getKey().equals("test")) {
                            throw new AssertionError("Key was modified");
                        }
                    }
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await(5, TimeUnit.SECONDS);

        if (!exceptions.isEmpty()) {
            throw new AssertionError("Thread safety test failed: " + exceptions.get(0).getMessage());
        }
    }

    @Test
    void testComplexEscapeSequences() {
        // Test multiple consecutive backslashes
        MetricName multiBackslash = MetricName.build("metric\\\\\\\\test");
        String stringRep = multiBackslash.toString();
        assertThat(stringRep).isEqualTo("metric\\\\\\\\\\\\\\\\test");

        MetricName parsed = MetricName.parse(stringRep);
        assertThat(parsed).isEqualTo(multiBackslash);
        assertThat(parsed.getKey()).isEqualTo("metric\\\\\\\\test");
    }

    @Test
    void testEscapeSequenceEdgeCases() {
        // Test backslash at end of string
        MetricName endBackslash = MetricName.build("test\\");
        String stringRep = endBackslash.toString();
        MetricName parsed = MetricName.parse(stringRep);
        assertThat(parsed).isEqualTo(endBackslash);

        // Test backslash followed by non-special character
        MetricName backslashLetter = MetricName.build("test\\x");
        stringRep = backslashLetter.toString();
        parsed = MetricName.parse(stringRep);
        assertThat(parsed).isEqualTo(backslashLetter);
    }

    @Test
    void testMemoryUsageWithDeepNesting() {
        // Test that deeply nested resolve operations don't cause memory issues
        MetricName name = MetricName.build("root");

        // Create a deeply nested metric name
        for (int i = 0; i < 100; i++) {
            name = name.resolve("level" + i);
        }

        String stringRep = name.toString();
        MetricName parsed = MetricName.parse(stringRep);
        assertThat(parsed).isEqualTo(name);

        // Verify the key is correctly constructed
        assertThat(name.getKey()).startsWith("root.level0");
        assertThat(name.getKey()).endsWith("level99");
    }

    @Test
    void testTagOrderingConsistency() {
        // Test that tag ordering is consistent across different creation methods
        Map<String, String> tags = Map.of(
                "zebra", "z",
                "alpha", "a",
                "beta", "b");

        MetricName fromMap = new MetricName("test", tags);
        MetricName fromChaining = MetricName.build("test")
                .tagged("zebra", "z")
                .tagged("alpha", "a")
                .tagged("beta", "b");

        assertThat(fromMap.toString()).isEqualTo(fromChaining.toString());
        assertThat(fromMap).isEqualTo(fromChaining);

        // Verify alphabetical ordering in string representation
        String stringRep = fromMap.toString();
        int alphaPos = stringRep.indexOf("alpha=a");
        int betaPos = stringRep.indexOf("beta=b");
        int zebraPos = stringRep.indexOf("zebra=z");

        assertThat(alphaPos).isLessThan(betaPos);
        assertThat(betaPos).isLessThan(zebraPos);
    }
}

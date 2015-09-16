package io.dropwizard.metrics.atsd;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UtilsTest {

    @Test
    public void sanitizeEntity() throws Exception {
        String[] inputs = {
                "testname",
                "test name",
                "'t'est name",
                "te\"stname\""
        };
        String[] expecteds = {
                "testname",
                "test_name",
                "test_name",
                "testname"
        };
        for(int i = 0; i < inputs.length; i++) {
            assertThat(Utils.sanitizeEntity(inputs[i])).isEqualTo(expecteds[i]);
        }
    }

    @Test
    public void sanitizeMetric() throws Exception {
        String[] inputs = {
                "testname",
                "test name",
                "'t'est name",
                "te\"stname\""
        };
        String[] expecteds = {
                "testname",
                "test_name",
                "test_name",
                "testname"
        };
        for(int i = 0; i < inputs.length; i++) {
            assertThat(Utils.sanitizeMetric(inputs[i])).isEqualTo(expecteds[i]);
        }
    }

    @Test
    public void sanitizeTagKey() throws Exception {
        String[] inputs = {
                "testname",
                "test name",
                "'t'est name",
                "te\"stname\""
        };
        String[] expecteds = {
                "testname",
                "test_name",
                "test_name",
                "testname"
        };
        for(int i = 0; i < inputs.length; i++) {
            assertThat(Utils.sanitizeTagKey(inputs[i])).isEqualTo(expecteds[i]);
        }
    }

    @Test
    public void sanitizeTagValue() throws Exception {
        String[] inputs = {
                "testname",
                "test name",
                "'t'e\"st name",
                "te\"s'tname\""

        };
        String[] expecteds = {
                "testname",
                "\"test name\"",
                "\"'t'e\\\"st name\"",
                "te\\\"s'tname\\\""
        };
        for(int i = 0; i < inputs.length; i++) {
            assertThat(Utils.sanitizeTagValue(inputs[i])).isEqualTo(expecteds[i]);
        }
    }

}

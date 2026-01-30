// SPDX-License-Identifier: Apache-2.0
package org.gradlex.javamodule.testing.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ModuleInfoParseTest {

    @Test
    void ignores_single_line_comments() {
        var nameFromFile = ModuleInfoParser.moduleName("""
            // module some.thing.else
            module some.thing {
                requires transitive foo.bar.la;
            }
            """);

        assertThat(nameFromFile).isEqualTo("some.thing");
    }

    @Test
    void ignores_single_line_comments_late_in_line() {
        var nameFromFile = ModuleInfoParser.moduleName("""
            module some.thing { // module some.thing.else
                requires transitive foo.bar.la;
            }
            """);

        assertThat(nameFromFile).isEqualTo("some.thing");
    }

    @Test
    void ignores_multi_line_comments() {
        var nameFromFile = ModuleInfoParser.moduleName("""
            /*
            module some.thing.else;
            */
            module some.thing {
                requires static foo.bar.la;
            }
            """);

        assertThat(nameFromFile).isEqualTo("some.thing");
    }

    @Test
    void ignores_multi_line_comments_between_keywords() {
        var nameFromFile = ModuleInfoParser.moduleName("""
            module /*module some.other*/ some.thing { /* module
            odd comment*/ requires transitive foo.bar.la;
                requires/* weird comment*/ static foo.bar.lo;
                requires /*something to say*/foo.bar.li; /*
                    requires only.a.comment
                */
            }
            """);

        assertThat(nameFromFile).isEqualTo("some.thing");
    }

    @Test
    void finds_module_name_when_open_keyword_is_used() {
        var nameFromFile = ModuleInfoParser.moduleName("""
            open module /*module some.other*/ some.thing { /* module
            odd comment*/ requires transitive foo.bar.la;
                requires/* weird comment*/ static foo.bar.lo;
                requires /*something to say*/foo.bar.li; /*
                    requires only.a.comment
                */
            }
            """);

        assertThat(nameFromFile).isEqualTo("some.thing");
    }
}

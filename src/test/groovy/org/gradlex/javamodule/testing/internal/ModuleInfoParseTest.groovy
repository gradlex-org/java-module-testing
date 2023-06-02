package org.gradlex.javamodule.testing.internal

import spock.lang.Specification

class ModuleInfoParseTest extends Specification {

    def "ignores single line comments"() {
        given:
        def nameFromFile = ModuleInfoParser.moduleName('''
            // module some.thing.else
            module some.thing {
                requires transitive foo.bar.la;
            }
        ''')

        expect:
        nameFromFile == 'some.thing'
    }

    def "ignores single line comments late in line"() {
        given:
        def nameFromFile = ModuleInfoParser.moduleName('''
            module some.thing { // module some.thing.else
                requires transitive foo.bar.la;
            }
        ''')

        expect:
        nameFromFile == 'some.thing'
    }

    def "ignores multi line comments"() {
        given:
        def nameFromFile = ModuleInfoParser.moduleName('''
            /*
            module some.thing.else;
            */
            module some.thing {
                requires static foo.bar.la;
            }
        ''')

        expect:
        nameFromFile == 'some.thing'
    }

    def "ignores multi line comments between keywords"() {
        given:
        def nameFromFile = ModuleInfoParser.moduleName('''
            module /*module some.other*/ some.thing { /* module
            odd comment*/ requires transitive foo.bar.la;
                requires/* weird comment*/ static foo.bar.lo;
                requires /*something to say*/foo.bar.li; /*
                    requires only.a.comment
                */
            }
        ''')

        expect:
        nameFromFile == 'some.thing'
    }
}

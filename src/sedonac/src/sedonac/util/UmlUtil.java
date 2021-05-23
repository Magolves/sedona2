package sedonac.util;

import sedona.util.TextUtil;
import sedonac.ast.Expr;
import sedonac.ast.FieldDef;
import sedonac.ir.IrField;
import sedonac.namespace.Field;
import sedonac.namespace.Slot;
import sedonac.namespace.Type;

public class UmlUtil {
    public static final String NL = System.getProperty("line.separator");
    public static final String INDENT = "  ";


    public static final String[] EMPTY_STRING_ARRAY = new String[]{};
    public static final String FACET_RANGE = "range";
    public static final String NO_INIT_STRING = "-";

    /**
     * Gets the plantUML prefix for the given slot scope/visibility.
     *
     * @param slot the slot to get the prefix for
     * @return the prefix string representing the visibility
     */
    public static String getScopePrefix(Slot slot) {
        if (slot.isPrivate()) return "-";
        if (slot.isProtected()) return "#";
        if (slot.isPublic()) return "+";

        return "~"; // else package-private
    }

    /**
     * Gets the initial (UI) value as string.
     * @param fieldDef the field to get the value for
     * @return the initial UI value
     */
    public static String getInitAsUiValue(FieldDef fieldDef) {
        String literal = getInitAsLiteral(fieldDef);

        switch ((fieldDef.type.id())) {
            case Type.boolId:
                // Hack
                return "true".equalsIgnoreCase(literal) ? "X" : "";

            case Type.byteId:
            case Type.shortId:
            case Type.intId:
                if (isEnum(fieldDef)) {
                    int index;

                    try {
                        index = Integer.parseInt(literal);
                    } catch (NumberFormatException e) {
                        // Symbolic literal -> keep 'as-is'
                        return literal;
                    }
                    String[] tags = getEnumLabels(fieldDef);
                    if (index < 0 || index >= tags.length) {
                        return String.format("<color:red>No tag for value %d (range is 0-%d)", index, (tags.length - 1));
                    } else {
                        return tags[index];
                    }
                } else {
                    return literal;
                }
            case Type.longId:
                if (literal.endsWith("L")) {
                    return literal.replace("L", "");
                } else {
                    return literal;
                }
            case Type.floatId:
            case Type.doubleId:
                if (literal.endsWith("F")) {
                    return literal.replace("F", "").replace(".", ",");
                } else {
                    return literal.replace(".", ",");
                }
            case Type.bufId:
                if (literal == null || literal.isEmpty()) {
                    return NO_INIT_STRING;
                } else {
                    return literal;
                }
            default:
                return literal ;
        }
    }

    /**
     * Gets the init string literal (which is usually a plain value).
     * If the referenced type is located in a different kit, we can only obtain
     * the symbolic constant, since it is an IR type.
     * @param fieldDef the field to check
     * @return the literal
     */
    public static String getInitAsLiteral(FieldDef fieldDef) {
        if (fieldDef.init != null) {
            // Literal: just return value
            if (fieldDef.init instanceof Expr.Literal) {
                Expr.Literal literal = (Expr.Literal) fieldDef.init;
                return String.valueOf(literal.value);
            } else if (fieldDef.init instanceof Expr.Field) {
                Expr.Field field = (Expr.Field) fieldDef.init;
                if (field.field instanceof FieldDef) {
                    return getInitAsLiteral((FieldDef) field.field);
                } else if (field.field instanceof IrField) {
                    return field.field.qname();
                }
            } else {
                return String.valueOf(fieldDef.init);
            }
        }

        return NO_INIT_STRING;
    }

    /**
     * Converts the given (camel-case) text into human readable form, e. g.
     * 'fooBarBaz' becomes 'Foo bar baz'.
     * @param text the text to convert in camel-case format
     * @return the converted text
     */
    public static String camelToHuman(String text) {
        StringBuilder stringBuilder = new StringBuilder();

        String[] tokens = text.split("(?=[A-Z])");
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (i > 0) {
                stringBuilder.append(" ").append(TextUtil.decapitalize(token));
            } else {
                stringBuilder.append(TextUtil.capitalize(token));
            }
        }

        return stringBuilder.toString();
    }

    /**
     * Checks if given type is primitive or an array of primitives.
     *
     * @param type the type to check
     * @return true, if (array) type is primitive
     */
    public static boolean isPrimitive(Type type) {
        return type.isPrimitive() || (type.isArray() && type.arrayOf().isPrimitive());
    }

    /**
     * Checks if given type is a Sedona type (primitive or a Str or a Buf) or an array of Sedona type.
     *
     * @param type the type to check
     * @return true, if (array) type is primitive
     */
    public static boolean isSedonaType(Type type) {
        return isPrimitive(type) || type.isStr() || type.isBuf() || (type.isArray() && (type.arrayOf().isStr() || type.arrayOf().isBuf()));
    }

    /**
     * Checks if given field is an enumeration ("range" facet is set).
     *
     * @param field the field to check
     * @return ture, if field is an enumeration
     */
    public static boolean isEnum(Field field) {
        if (field instanceof FieldDef) {
            FieldDef fieldDef = (FieldDef) field;
            return fieldDef.facets().gets(FACET_RANGE) != null;
        }

        return false;
    }

    /**
     * Checks if field/property is marked as readonly.
     * @param fieldDef the field to check
     * @return true, if property is read-only
     */
    public static boolean isReadOnly(FieldDef fieldDef) {
        if (fieldDef.facets().isEmpty()) return false;

        return fieldDef.facets().getb("readonly");
    }

    /**
     * gets the enum tags for the given field.
     * @param field the field to get the enums for,
     * @return the array containing the tags or an empty array, if no tags are present (or cannot be
     * obtained because the field is an IR type).
     */
    public static String[] getEnumLabels(Field field) {
        if (field instanceof FieldDef) {
            FieldDef fieldDef = (FieldDef) field;
            String enums = fieldDef.facets().gets("range");

            return enums.split(",");
        }

        return EMPTY_STRING_ARRAY;
    }
}

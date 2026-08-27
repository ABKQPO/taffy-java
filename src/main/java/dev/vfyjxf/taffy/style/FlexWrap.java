package dev.vfyjxf.taffy.style;

import java.util.Locale;

/**
 * Controls whether flex items are forced onto one line or can wrap onto multiple lines.
 * 
 * @see <a href="https://www.w3.org/TR/css-flexbox-1/#flex-wrap-property">CSS Flexbox - flex-wrap</a>
 */
public enum FlexWrap {
    /** Items will not wrap and stay on a single line */
    NO_WRAP,
    
    /** Items will wrap according to this item's FlexDirection */
    WRAP,
    
    /** Items will wrap in the opposite direction to this item's FlexDirection */
    WRAP_REVERSE,

    /** Items wrap into balanced lines with the smallest possible largest line. */
    BALANCE,

    /** Items wrap into balanced reverse lines. */
    BALANCE_REVERSE;

    /** Returns true when this mode permits more than one flex line. */
    public boolean isMultiLine() {
        return this != NO_WRAP;
    }

    /** Returns true when lines are stacked in the reverse cross-axis direction. */
    public boolean isReverse() {
        return this == WRAP_REVERSE || this == BALANCE_REVERSE;
    }

    /** Returns true when this mode requests balanced line breaking. */
    public boolean isBalance() {
        return this == BALANCE || this == BALANCE_REVERSE;
    }

    /** Parse the CSS flex-wrap grammar, including Flexbox Level 2 balance keywords. */
    public static FlexWrap parse(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new ParseError("Flex-wrap value must not be empty");
        }
        String[] tokens = value.trim().toLowerCase(Locale.ROOT).split("\\s+");
        FlexWrap wrap = null;
        boolean balance = false;
        for (String token : tokens) {
            switch (token) {
                case "nowrap":
                    if (tokens.length != 1 || wrap != null || balance) {
                        throw new ParseError("Invalid flex-wrap value: " + value);
                    }
                    return NO_WRAP;
                case "wrap":
                    if (wrap != null) throw new ParseError("Duplicate flex-wrap mode: wrap");
                    wrap = WRAP;
                    break;
                case "wrap-reverse":
                    if (wrap != null) throw new ParseError("Duplicate flex-wrap mode: wrap-reverse");
                    wrap = WRAP_REVERSE;
                    break;
                case "balance":
                    if (balance) throw new ParseError("Duplicate flex-wrap mode: balance");
                    balance = true;
                    break;
                default:
                    throw new ParseError("Unknown flex-wrap keyword: " + token);
            }
        }
        if (!balance) return wrap == null ? NO_WRAP : wrap;
        return wrap == WRAP_REVERSE ? BALANCE_REVERSE : BALANCE;
    }

    /** Return the CSS keyword representation. */
    @Override
    public String toString() {
        return switch (this) {
            case NO_WRAP -> "nowrap";
            case WRAP -> "wrap";
            case WRAP_REVERSE -> "wrap-reverse";
            case BALANCE -> "wrap balance";
            case BALANCE_REVERSE -> "wrap-reverse balance";
            default -> throw new IllegalStateException("Unexpected flex-wrap mode: " + this);
        };
    }
}

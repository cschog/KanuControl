import { Typography, TypographyProps, SxProps, Theme } from "@mui/material";
import { fontSize } from "@/theme/ui";

type MoneyProps = {
  value: number;
  align?: "left" | "right" | "center";
  colorize?: boolean;
  color?: TypographyProps["color"];
  sx?: SxProps<Theme>;
} & Pick<TypographyProps, "variant">;

const formatter = new Intl.NumberFormat("de-DE", {
  style: "currency",
  currency: "EUR",
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

const Money = ({ value, align = "right", colorize = false, color, variant, sx }: MoneyProps) => {
  const resolvedColor =
    color ?? (colorize && value !== 0 ? (value > 0 ? "success.main" : "error.main") : "inherit");

  return (
    <Typography
      component="span"
      variant={variant}
      sx={{
        textAlign: align,
        color: resolvedColor,
        fontVariantNumeric: "tabular-nums",
        fontSize: fontSize.money,
        ...sx,
      }}
    >
      {formatter.format(value)}
    </Typography>
  );
};

export default Money;

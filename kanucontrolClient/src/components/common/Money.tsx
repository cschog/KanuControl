import { Typography, TypographyProps } from "@mui/material";

type MoneyProps = {
  value: number;
  align?: "left" | "right" | "center";
  colorize?: boolean;
} & Pick<TypographyProps, "variant">;

const formatter = new Intl.NumberFormat("de-DE", {
  style: "currency",
  currency: "EUR",
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

const Money = ({ value, align = "right", colorize = false, variant = "body1" }: MoneyProps) => {
  const color = colorize && value !== 0 ? (value > 0 ? "success.main" : "error.main") : "inherit";

  return (
    <Typography
      variant={variant}
      sx={{
        textAlign: align,
        color,
        fontVariantNumeric: "tabular-nums",
      }}
    >
      {formatter.format(value)}
    </Typography>
  );
};

export default Money;

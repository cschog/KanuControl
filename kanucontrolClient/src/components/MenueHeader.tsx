import React, { memo } from "react";

interface MenueHeaderProps {
  headerText?: string;
}

const MenueHeaderComponent: React.FC<MenueHeaderProps> = ({ headerText }) => {
  return (
    <header>
      <h2>{headerText}</h2>
    </header>
  );
};

export const MenueHeader = memo(MenueHeaderComponent);
import React, { memo } from "react";
import PropTypes from "prop-types";

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

MenueHeaderComponent.propTypes = {
  headerText: PropTypes.string,
};

export const MenueHeader = memo(MenueHeaderComponent);

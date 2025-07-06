import React from "react";

const ArmenianPinLogo: React.FC<{ size?: number }> = ({ size = 100 }) => (
  <svg
    width={size}
    height={size}
    viewBox="0 0 100 100"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
  >
    {/* Outer pin shape */}
    <path
      d="M50 0C77 0 100 23 100 50C100 77 50 100 50 100C50 100 0 77 0 50C0 23 23 0 50 0Z"
      fill="#F2F2F2"
    />
    {/* Red top */}
    <path d="M50 0C77 0 100 23 100 50H0C0 23 23 0 50 0Z" fill="#D90012" />
    {/* Blue middle */}
    <path d="M0 50H100V66.66H0V50Z" fill="#0033A0" />
    {/* Orange bottom */}
    <path
      d="M0 66.66H100C100 77 50 100 50 100C50 100 0 77 0 66.66Z"
      fill="#F2A800"
    />
    {/* Inner white circle */}
    <circle cx="50" cy="50" r="20" fill="white" />
  </svg>
);

export default ArmenianPinLogo;

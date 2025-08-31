import React, { useState, useRef, useLayoutEffect } from 'react';
import { TextField, InputAdornment, IconButton } from '@mui/material';
import { Visibility, VisibilityOff } from '@mui/icons-material';

interface PasswordFieldProps {
  label: string;
  value: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  error?: boolean;
  helperText?: string | null;
  required?: boolean;
  fullWidth?: boolean;
  margin?: 'none' | 'dense' | 'normal';
  autoComplete?: string;
}

export const PasswordField: React.FC<PasswordFieldProps> = ({
  label,
  value,
  onChange,
  error = false,
  helperText,
  required = false,
  fullWidth = true,
  margin = 'normal',
  autoComplete = 'current-password',
}) => {
  const [show, setShow] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);

  // preserve selection, not just position
  const selRef = useRef<{ start: number; end: number }>({ start: 0, end: 0 });

  const saveSelection = () => {
    const el = inputRef.current;
    if (!el) return;
    selRef.current = {
      start: el.selectionStart ?? 0,
      end: el.selectionEnd ?? 0,
    };
  };

  const restoreSelection = () => {
    const el = inputRef.current;
    if (!el) return;
    const { start, end } = selRef.current;
    // rAF ensures that type change has already been applied
    requestAnimationFrame(() => {
      try {
        el.setSelectionRange(start, end);
      } catch { /* ignore for password on some devices */ }
      el.focus({ preventScroll: true });
    });
  };

  const toggle = () => {
    saveSelection();
    setShow((v) => !v);
  };

  useLayoutEffect(() => {
    restoreSelection();
  }, [show]);

  return (
    <TextField
      label={label}
      type={show ? 'text' : 'password'}
      value={value}
      onChange={onChange}
      error={error}
      helperText={helperText}
      required={required}
      fullWidth={fullWidth}
      margin={margin}
      inputRef={inputRef}
      autoComplete={autoComplete}
      sx={{
        width: '100%',
        '& .MuiOutlinedInput-root': {
          borderRadius: '10px',
          '& .MuiOutlinedInput-notchedOutline': { borderColor: 'rgba(0,0,0,0.23)' },
          '&:hover .MuiOutlinedInput-notchedOutline': { borderColor: 'rgba(0,0,0,0.6)' },
          '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
            borderColor: 'rgba(0,0,0,0.6)',
            borderWidth: '2px',
          },
        },
      }}
      slotProps={{
        input: {
          endAdornment: (
            <InputAdornment position="end" sx={{ m: 0 }}>
              <IconButton
                aria-label={show ? 'hide password' : 'show password'}
                onClick={toggle}
                // important: completely block focus transfer to the button
                onMouseDown={(e) => e.preventDefault()}
                onPointerDown={(e) => e.preventDefault()}
                edge="end"
                tabIndex={-1}
                disableRipple
                disableFocusRipple
              >
                {show ? <VisibilityOff /> : <Visibility />}
              </IconButton>
            </InputAdornment>
          ),
        },
      }}
    />
  );
};

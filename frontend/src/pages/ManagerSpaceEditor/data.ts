import PALETTE from 'constants/palette';
import { Area } from 'types/common';
import { SpaceEditorMode } from 'types/editor';
import { formatDate, formatTimeWithSecond } from 'utils/datetime';

export interface SpaceFormValue {
  name: string;
  color: string;
  availableStartTime: string;
  availableEndTime: string;
  reservationTimeUnit: string | number;
  reservationMinimumTimeUnit: string | number;
  reservationMaximumTimeUnit: string | number;
  reservationEnable: boolean;
  enabledWeekdays: {
    monday: boolean;
    tuesday: boolean;
    wednesday: boolean;
    thursday: boolean;
    friday: boolean;
    saturday: boolean;
    sunday: boolean;
  };
  area: Area | null;
}

const today = formatDate(new Date());

export const initialSpaceFormValue: Omit<SpaceFormValue, 'enabledWeekdays' | 'area'> = {
  reservationEnable: true,
  name: '',
  color: PALETTE.RED[500],
  availableStartTime: formatTimeWithSecond(new Date(`${today}T07:00:00`)),
  availableEndTime: formatTimeWithSecond(new Date(`${today}T23:00:00`)),
  reservationTimeUnit: '10',
  reservationMinimumTimeUnit: '10',
  reservationMaximumTimeUnit: '1440',
};

export const initialEnabledWeekdays: SpaceFormValue['enabledWeekdays'] = {
  monday: true,
  tuesday: true,
  wednesday: true,
  thursday: true,
  friday: true,
  saturday: true,
  sunday: true,
};

export const colorSelectOptions = [
  PALETTE.RED[500],
  PALETTE.ORANGE[500],
  PALETTE.YELLOW[500],
  PALETTE.GREEN[500],
  PALETTE.BLUE[300],
  PALETTE.BLUE[900],
  PALETTE.PURPLE[500],
];

export const timeUnits = ['5', '10', '30', '60'];

export const drawingModes = [SpaceEditorMode.Rect, SpaceEditorMode.Polygon];

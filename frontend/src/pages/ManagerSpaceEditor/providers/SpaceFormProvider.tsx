import React, { createContext, Dispatch, ReactNode, SetStateAction, useState } from 'react';
import useInputs from 'hooks/useInputs';
import { Area, ManagerSpace, ManagerSpaceAPI } from 'types/common';
import { WithOptional } from 'types/util';
import { formatDate, formatTimeWithSecond } from 'utils/datetime';
import { initialEnabledWeekdays, initialSpaceFormValue, SpaceFormValue } from '../data';

interface Props {
  children: ReactNode;
}

export interface SpaceProviderValue {
  values: SpaceFormValue;
  onChange: (event: React.ChangeEvent<HTMLInputElement>) => void;
  resetForm: () => void;
  updateArea: (nextArea: Area) => void;
  updateWithSpace: (space: ManagerSpace) => void;
  setValues: (nextValue: SpaceFormValue) => void;
  getRequestValues: () => {
    space: WithOptional<ManagerSpaceAPI, 'id'>;
  };
  selectedPresetId: number | null;
  setSelectedPresetId: Dispatch<SetStateAction<number | null>>;
}

export const SpaceFormContext = createContext<SpaceProviderValue | null>(null);
const weekdays = Object.keys(initialEnabledWeekdays);

const SpaceFormProvider = ({ children }: Props): JSX.Element => {
  const [spaceFormValue, onChangeSpaceFormValues, setSpaceFormValues] =
    useInputs(initialSpaceFormValue);
  const [enabledWeekdays, onChangeEnabledWeekdays, setEnabledWeekdays] =
    useInputs(initialEnabledWeekdays);
  const [area, setArea] = useState<Area | null>(null);
  const [selectedPresetId, setSelectedPresetId] = useState<number | null>(null);

  const values = { ...spaceFormValue, enabledWeekdays, area };

  const setValues = (values: SpaceFormValue) => {
    setEnabledWeekdays({ ...values.enabledWeekdays });
    setArea(values.area === null ? null : { ...values.area });

    const nextValues = { ...values };

    delete (nextValues as WithOptional<SpaceFormValue, 'enabledWeekdays'>).enabledWeekdays;
    delete (nextValues as WithOptional<SpaceFormValue, 'area'>).area;

    setSpaceFormValues(nextValues);
  };

  const updateWithSpace = (space: ManagerSpace) => {
    const { name, color, area, settings } = space;

    const enableWeekdays = settings.enabledDayOfWeek?.split(',') ?? [];
    const nextEnableWeekdays: { [key: string]: boolean } = {};
    Object.keys(values.enabledWeekdays).forEach(
      (weekday) => (nextEnableWeekdays[weekday] = enableWeekdays.includes(weekday))
    );

    setValues({
      name,
      color,
      ...settings,
      enabledWeekdays: nextEnableWeekdays as SpaceFormValue['enabledWeekdays'],
      area,
    });
  };

  const updateArea = (nextArea: Area) => {
    setArea(nextArea);
    setSpaceFormValues(initialSpaceFormValue);
    setEnabledWeekdays(initialEnabledWeekdays);
  };

  const getRequestValues = () => {
    const todayDate = formatDate(new Date());

    const availableStartTime = formatTimeWithSecond(
      new Date(`${todayDate}T${values.availableStartTime}`)
    );
    const availableEndTime = formatTimeWithSecond(
      new Date(`${todayDate}T${values.availableEndTime}`)
    );

    const enabledDayOfWeek = Object.keys(values.enabledWeekdays)
      .filter(
        (weekday) => values.enabledWeekdays[weekday as keyof SpaceFormValue['enabledWeekdays']]
      )
      .join();

    return {
      space: {
        name: values.name,
        color: values.color,
        description: values.name,
        area: JSON.stringify(values.area),
        settings: {
          availableStartTime,
          availableEndTime,
          reservationTimeUnit: Number(values.reservationTimeUnit),
          reservationMinimumTimeUnit: Number(values.reservationMinimumTimeUnit),
          reservationMaximumTimeUnit: Number(values.reservationMaximumTimeUnit),
          reservationEnable: values.reservationEnable,
          enabledDayOfWeek,
        },
      },
    };
  };

  const onChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (selectedPresetId !== null) setSelectedPresetId(null);

    if (weekdays.includes(event.target.name)) {
      onChangeEnabledWeekdays(event);

      return;
    }

    onChangeSpaceFormValues(event);
  };

  const resetForm = () => {
    setValues({ ...initialSpaceFormValue, enabledWeekdays: initialEnabledWeekdays, area: null });
  };

  return (
    <SpaceFormContext.Provider
      value={{
        values,
        onChange,
        resetForm,
        updateArea,
        setValues,
        updateWithSpace,
        getRequestValues,
        selectedPresetId,
        setSelectedPresetId,
      }}
    >
      {children}
    </SpaceFormContext.Provider>
  );
};

export default SpaceFormProvider;

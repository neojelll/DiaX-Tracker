import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it } from 'vitest'
import { ToastProvider } from '@/components/ui/toast'
import { db } from '@/diary/db'
import { diary } from '@/diary/repository'
import { FoodScreen } from '@/screens/food/FoodScreen'
import { EMPTY_FORM, formStore } from '@/screens/home/entryForm'
import { HomeScreen } from '@/screens/home/HomeScreen'

const wrap = (ui: React.ReactNode) => render(<ToastProvider>{ui}</ToastProvider>)

describe('FoodScreen', () => {
  beforeEach(async () => {
    formStore.set(EMPTY_FORM)
    await Promise.all([db.entries.clear(), db.presets.clear(), db.values.clear()])
  })

  it('creates a preset with its products', async () => {
    wrap(<FoodScreen />)
    expect(await screen.findByText('0 пресетов')).toBeInTheDocument()
    await userEvent.click(screen.getByRole('button', { name: 'Новый приём пищи' }))
    const sheet = screen.getByRole('dialog', { name: 'Добавить приём пищи' })
    const save = within(sheet).getByRole('button', { name: 'Сохранить' })
    expect(save).toBeDisabled()
    await userEvent.type(within(sheet).getByLabelText('Название'), 'Гречка с курицей')
    await userEvent.type(within(sheet).getByLabelText('Продукт 1'), 'Гречка')
    await userEvent.type(within(sheet).getByLabelText('Углеводы продукта 1, ХЕ'), '4')
    await userEvent.click(within(sheet).getByRole('button', { name: 'Добавить продукт' }))
    await userEvent.type(within(sheet).getByLabelText('Продукт 2'), 'Курица')
    expect(within(sheet).getByText('Итого').nextSibling).toHaveTextContent('4 ХЕ')
    await userEvent.click(save)

    await waitFor(() => expect(screen.getByRole('status')).toHaveTextContent('Приём пищи создан'))
    expect(await screen.findByText('1 пресет')).toBeInTheDocument()
    // The name in full, not cut short (#222).
    expect(screen.getByRole('button', { name: /Гречка с курицей/ })).toBeInTheDocument()
    expect(await diary.allPresets()).toMatchObject([
      {
        name: 'Гречка с курицей',
        products: [
          { name: 'Гречка', carbsGrams: 40 },
          { name: 'Курица', carbsGrams: 0 },
        ],
      },
    ])
  })

  it('picks a preset for the entry form, and deleting one can be undone', async () => {
    await diary.savePreset({
      name: 'Овсянка',
      comment: 'дома',
      products: [{ name: 'Хлопья', carbsGrams: 30 }],
    })
    wrap(<FoodScreen />)
    await userEvent.click(await screen.findByRole('button', { name: /Овсянка/ }))
    await userEvent.click(screen.getByRole('button', { name: 'Выбрать для записи' }))
    expect(formStore.get()).toMatchObject({ mealLabel: 'Овсянка', carbs: '3' })
    expect(window.location.hash).toBe('#/entry')

    await userEvent.click(screen.getByRole('button', { name: /Овсянка/ }))
    await userEvent.click(screen.getByRole('button', { name: 'Удалить' }))
    await waitFor(async () => expect(await diary.allPresets()).toHaveLength(0))
    await userEvent.click(screen.getByRole('button', { name: 'Вернуть' }))
    await waitFor(async () => expect(await diary.allPresets()).toHaveLength(1))
  })

  it('a preset made from the home form goes straight into it', async () => {
    wrap(<HomeScreen />)
    await userEvent.click(screen.getByRole('button', { name: /Еда/ }))
    await userEvent.click(screen.getByRole('button', { name: 'Новый пресет' }))
    const sheet = screen.getByRole('dialog', { name: 'Добавить приём пищи' })
    await userEvent.type(within(sheet).getByLabelText('Название'), 'Сырники')
    await userEvent.type(within(sheet).getByLabelText('Продукт 1'), 'Творог')
    await userEvent.type(within(sheet).getByLabelText('Углеводы продукта 1, ХЕ'), '2,5')
    await userEvent.click(within(sheet).getByRole('button', { name: 'Сохранить' }))
    await waitFor(() => expect(screen.getByLabelText('Углеводы, ХЕ')).toHaveValue('2,5'))
    expect(screen.getByRole('button', { name: /Еда/ })).toHaveTextContent('Сырники')
  })
})

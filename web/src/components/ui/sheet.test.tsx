import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { useState } from 'react'
import { describe, expect, it } from 'vitest'
import { Sheet } from '@/components/ui/sheet'

function Harness() {
  const [open, setOpen] = useState(false)
  return (
    <>
      <button onClick={() => setOpen(true)}>Открыть</button>
      <Sheet open={open} onOpenChange={setOpen} title="Время" description="Подпись">
        <input aria-label="Поле" />
      </Sheet>
    </>
  )
}

describe('Sheet', () => {
  it('opens as a named dialog without focusing a field', async () => {
    render(<Harness />)
    await userEvent.click(screen.getByRole('button', { name: 'Открыть' }))
    const dialog = screen.getByRole('dialog', { name: 'Время' })
    expect(dialog).toHaveAccessibleDescription('Подпись')
    // A focused field would throw the phone keyboard up the moment the sheet opens.
    expect(screen.getByRole('textbox', { name: 'Поле' })).not.toHaveFocus()
  })

  it('closes with its × and with Escape', async () => {
    render(<Harness />)
    await userEvent.click(screen.getByRole('button', { name: 'Открыть' }))
    await userEvent.click(screen.getByRole('button', { name: 'Закрыть' }))
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()

    await userEvent.click(screen.getByRole('button', { name: 'Открыть' }))
    await userEvent.keyboard('{Escape}')
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })
})

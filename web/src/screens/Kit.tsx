import { useState } from 'react'
import { Icon, InsulinIcon } from '@/components/icons'
import { ICON_PATHS, type IconName } from '@/components/iconPaths'
import { Button, Chip, CircleButton } from '@/components/ui/button'
import { Badge, Capsule, NumberWithUnit } from '@/components/ui/capsule'
import { Field } from '@/components/ui/field'
import { Sheet } from '@/components/ui/sheet'
import { Stepper } from '@/components/ui/stepper'
import { Card, Divider, Kicker, Tile } from '@/components/ui/surface'
import { GLUCOSE_TEXT, glucoseLevel } from '@/diary/glucose'
import { numeric } from '@/lib/numeric'
import { useToast } from '@/lib/toast'

const RANGE = { low: 3.9, high: 10 }

/**
 * Every piece of the design system on one page, to check them on a real phone - the dev build only
 * (#/kit). Not a screen of the app.
 */
export default function Kit() {
  const toast = useToast()
  const [sheet, setSheet] = useState<'plain' | 'form' | 'long' | null>(null)
  const [chip, setChip] = useState('all')
  const [hours, setHours] = useState(4)
  const [low, setLow] = useState(3.9)
  const [sugar, setSugar] = useState('')
  const [note, setNote] = useState('')

  return (
    <section className="space-y-3 px-3.5 pt-4">
      <h1 className="text-screen-title">Компоненты</h1>
      <p className="text-body text-text-secondary">Витрина дизайн-системы, только в dev-сборке.</p>

      <Card className="space-y-3">
        <Kicker>Шрифт</Kicker>
        <p className="text-display-sugar text-sugar-in-range tabular-nums">6.4</p>
        <p className="text-display-insulin tabular-nums">4 ЕД</p>
        <p className="text-value-large">Value large 20</p>
        <p className="text-value">Value 17</p>
        <p className="text-value-small">Value small 15</p>
        <p className="text-record-time tabular-nums">09:41</p>
        <p className="text-body">Body 13.5 - обычный текст записи и подписей.</p>
        <p className="text-note text-text-secondary">Note 12.5 - пояснение под настройкой.</p>
        <p className="text-hint text-text-tertiary">Hint 11.5</p>
        <p className="text-card-label text-text-label">Card label 10.5</p>
      </Card>

      <Card className="space-y-3">
        <Kicker>
          Сахар по диапазону {RANGE.low}–{RANGE.high}
        </Kicker>
        <div className="flex justify-between">
          {[2.8, 3.5, 6.4, 12.1, 15.2].map((v) => (
            <span key={v} className={`text-value-large tabular-nums ${GLUCOSE_TEXT[glucoseLevel(v, RANGE)]}`}>
              {v.toFixed(1)}
            </span>
          ))}
        </div>
      </Card>

      <Card className="space-y-3">
        <Kicker>Капсулы</Kicker>
        <div className="flex flex-wrap gap-1.5">
          <Capsule label="4 ХЕ" icon={<Icon name="dish" size={14} strokeWidth={2} />}>
            <NumberWithUnit number="4" unit="ХЕ" />
          </Capsule>
          <Capsule label="6 ЕД, короткий" icon={<InsulinIcon kind="short" size={14} />}>
            <NumberWithUnit number="6" unit="ЕД" />
          </Capsule>
          <Capsule label="18 ЕД, длинный" icon={<InsulinIcon kind="long" size={14} />}>
            <NumberWithUnit number="18" unit="ЕД" />
          </Capsule>
          <span className="rounded-strip bg-tile p-1.5">
            <Badge>2.5 ХЕ</Badge>
          </span>
        </div>
        <Divider />
        <div className="flex gap-1.5 rounded-tile bg-screen p-2">
          {['all', 'meals', 'insulin'].map((id) => (
            <Chip key={id} selected={chip === id} onClick={() => setChip(id)}>
              {{ all: 'Все', meals: 'Еда', insulin: 'Инсулин' }[id]}
            </Chip>
          ))}
        </div>
      </Card>

      <Card className="space-y-2.5">
        <Kicker>Плитки и числа</Kicker>
        <div className="grid grid-cols-2 gap-2.5">
          <Stepper
            label="Нижняя граница"
            value={low}
            step={0.1}
            min={3.9}
            max={15}
            onChange={setLow}
            format={(v) => v.toFixed(1)}
          />
          <Stepper
            label="Действие инсулина"
            value={hours}
            step={0.5}
            min={3.5}
            max={5.5}
            onChange={setHours}
            format={(v) => `${v.toFixed(1)} ч`}
          />
        </div>
        <Tile onClick={() => toast.show('Плитка нажата')}>
          <p className="text-card-label text-text-label">Нажимаемая плитка</p>
          <p className="mt-1 text-value tabular-nums">12:30</p>
        </Tile>
        <Field
          inputMode="decimal"
          placeholder="Сахар, ммоль/л"
          value={sugar}
          onChange={(e) => setSugar(numeric(e.target.value))}
        />
      </Card>

      <Card className="space-y-2.5">
        <Kicker>Кнопки</Kicker>
        <Button onClick={() => toast.show('Запись сохранена', { sub: 'Сегодня, 09:41' })}>
          Сохранить
          <Icon name="arrowRight" size={16} strokeWidth={2} />
        </Button>
        <div className="flex flex-wrap items-center gap-2">
          <Button
            variant="secondary"
            onClick={() => toast.show('Не получилось', { sub: 'Файл не похож на бэкап', kind: 'error' })}
          >
            <Icon name="alertTriangle" size={15} />
            Ошибка
          </Button>
          <CircleButton aria-label="Поиск" onClick={() => toast.show('Поиск')}>
            <Icon name="search" size={15} strokeWidth={2} />
          </CircleButton>
          <CircleButton tone="ink" aria-label="Добавить" onClick={() => toast.show('Добавить')}>
            <Icon name="plus" size={15} strokeWidth={2.2} />
          </CircleButton>
        </div>
      </Card>

      <Card className="space-y-2.5">
        <Kicker>Шторки и тосты</Kicker>
        <Button variant="secondary" className="w-full" onClick={() => setSheet('plain')}>
          Простая шторка
        </Button>
        <Button variant="secondary" className="w-full" onClick={() => setSheet('form')}>
          Шторка с полями (клавиатура)
        </Button>
        <Button variant="secondary" className="w-full" onClick={() => setSheet('long')}>
          Длинная шторка (прокрутка)
        </Button>
        <Button
          variant="secondary"
          className="w-full"
          onClick={() =>
            toast.show('Запись удалена', { sub: 'Сегодня, 08:15', kind: 'delete', onUndo: () => {} })
          }
        >
          Удалить с отменой
        </Button>
      </Card>

      <Card className="space-y-3">
        <Kicker>Иконки</Kicker>
        <div className="grid grid-cols-6 gap-3 text-ink">
          {(Object.keys(ICON_PATHS) as IconName[]).map((name) => (
            <div key={name} className="flex flex-col items-center gap-1">
              <Icon name={name} size={22} />
              <span className="w-full truncate text-center text-[8.5px] text-text-tertiary">{name}</span>
            </div>
          ))}
        </div>
      </Card>

      <Sheet
        open={sheet === 'plain'}
        onOpenChange={(o) => !o && setSheet(null)}
        title="Время"
        description="Тяните за верх, чтобы закрыть"
      >
        <p className="mb-4 text-display-insulin tabular-nums">09:40</p>
        <Button onClick={() => setSheet(null)}>Готово</Button>
      </Sheet>

      <Sheet open={sheet === 'form'} onOpenChange={(o) => !o && setSheet(null)} title="Новый пресет">
        <div className="space-y-2.5">
          <Field placeholder="Название" />
          <Field inputMode="decimal" placeholder="Углеводы, г" />
          <Field placeholder="Заметка" value={note} onChange={(e) => setNote(e.target.value)} />
          <Button
            onClick={() => {
              setSheet(null)
              toast.show('Пресет создан', { sub: note || 'Без названия' })
            }}
          >
            Сохранить
          </Button>
        </div>
      </Sheet>

      <Sheet open={sheet === 'long'} onOpenChange={(o) => !o && setSheet(null)} title="Продукты">
        <ul className="space-y-1.5">
          {Array.from({ length: 30 }, (_, i) => (
            <li key={i} className="flex justify-between rounded-strip bg-tile px-3 py-2.5 text-body">
              <span>Продукт {i + 1}</span>
              <span className="text-text-tertiary tabular-nums">{(i * 3) % 40} г</span>
            </li>
          ))}
        </ul>
      </Sheet>
    </section>
  )
}

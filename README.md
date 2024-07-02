# Function
A plugin to help support clan bingo events through runelite. This takes in a Json string defining all the items of the bingo and when one of the items is gained it will send a screenshot to discord through a web hook.

# The Bingo String
The bingo string is the json string and here is an example of the string.
```{
  "name": "Annual Sports Day",
  "webhook": "http://example.com/webhook",
  "teams": [
    {
      "name": "Team A",
      "players": [
        "Alice",
        "Bob",
        "Charlie"
      ],
      "current_tile": 3
    },
    {
      "name": "Team B",
      "players": [
        "Evil 478",
        "Eve",
        "Frank"
      ],
      "current_tile": 5
    }
  ],
  "board": {
    "tiles": [
      {
        "items": [
          "mind rune",
          "item6"
        ]
      },
      {
        "items": [
          "death rune"
        ]
      },
      {
        "go_to": 2
      },
      {
        "items": [
          "item8",
          "blood rune"
        ]
      }
    ]
  }
}```
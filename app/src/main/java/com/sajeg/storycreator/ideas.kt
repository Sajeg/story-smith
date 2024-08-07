package com.sajeg.storycreator

import androidx.compose.runtime.Composable

@Composable
fun getIdeas(count: Int): List<String> {
    val ideas: Array<String> = arrayOf(
        "A forgotten diary holds the key to a long-lost civilization.",
        "A small town is terrorized by a shape-shifting creature.",
        "A time traveler arrives with a warning about a future catastrophe.",
        "An amnesiac wakes up with no memory and a mysterious tattoo.",
        "A detective uncovers a conspiracy within the police force.",
        "A young girl inherits a haunted house with a deadly secret.",
        "A space crew discovers a sentient alien life form.",
        "A reclusive writer becomes entangled in a real-life murder mystery.",
        "A world government bans all forms of creativity.",
        "A hidden underground city thrives in a post-apocalyptic world.",
        "A group of friends witness a supernatural event.",
        "A scientist develops a serum that grants eternal life.",
        "A robot falls in love with its human creator.",
        "A parallel universe exists where everyone has superpowers.",
        "A detective investigates a series of impossible murders.",
        "A dying planet sends out a distress signal.",
        "A hidden treasure map leads to a dangerous adventure.",
        "A mysterious virus turns people into mindless zombies.",
        "A psychic predicts a devastating natural disaster.",
        "A secret society manipulates world events from the shadows.",
        "A time loop traps a woman in the same day repeatedly.",
        "A child discovers a portal to a magical realm.",
        "A renowned artist creates paintings that come to life.",
        "A deep-sea expedition uncovers a prehistoric creature.",
        "A government experiment goes horribly wrong.",
        "A lone survivor fights for humanity against an alien invasion.",
        "A young woman develops the ability to control dreams.",
        "A forgotten legend comes to life with terrifying consequences.",
        "A detective discovers a conspiracy involving extraterrestrials.",
        "A world government is controlled by artificial intelligence."
    )

    return ideas.toList().shuffled().take(count)
}
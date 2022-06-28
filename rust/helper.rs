use std::clone::Clone;

pub fn make<T: Clone>(size: usize, def: T) -> Vec<T> {
    let mut res = Vec::with_capacity(size);
    res.resize(size, def);
    return res;
}

fn main() {
    let arr = make::<i32>(5, 0);
    assert!(arr.len() == 5);

    let arr = make::<Option<String>>(5, None);
    assert!(arr.len() == 5);

    let a2 = make::<Vec<i32>>(5, make::<i32>(3, 0));
    assert!(a2.len() == 5);
    assert!(a2[0].len() == 3);
}